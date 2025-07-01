package com.sessionbuilder.it;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionbuilder.core.EmfFactory;
import com.sessionbuilder.core.SessionViewCallback;
import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionController;
import com.sessionbuilder.core.StudySessionInterface;
import com.sessionbuilder.core.StudySessionRepository;
import com.sessionbuilder.core.StudySessionRepositoryInterface;
import com.sessionbuilder.core.StudySessionService;
import com.sessionbuilder.core.Topic;
import com.sessionbuilder.core.TopicController;
import com.sessionbuilder.core.TopicRepository;
import com.sessionbuilder.core.TopicRepositoryInterface;
import com.sessionbuilder.core.TopicService;
import com.sessionbuilder.core.TopicServiceInterface;
import com.sessionbuilder.core.TopicViewCallback;
import com.sessionbuilder.core.TransactionManager;
import com.sessionbuilder.core.TransactionManagerImpl;
import com.sessionbuilder.swing.TopicAndSessionManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@RunWith(GUITestRunner.class)
public class TopicAndSessionManagerIT extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	private TopicAndSessionManager managerView;
	private EntityManagerFactory emf;
	private TopicController topicController;
	private StudySessionController sessionController;
	private TransactionManager transactionManager;
	
	@SuppressWarnings("resource")
	@ClassRule
	public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName(System.getenv().getOrDefault("POSTGRES_DB", "test"))
			.withUsername(System.getenv().getOrDefault("POSTGRES_USER", "test"))
			.withPassword(System.getenv().getOrDefault("POSTGRES_PASSWORD", "test"));
	
	@BeforeClass
	public static void setUpContainer() {
		postgres.start();
	}
	
	private void cleanDatabase() {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.createNativeQuery("TRUNCATE TABLE topic_studysession CASCADE").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE studysession CASCADE").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE topic CASCADE").executeUpdate();
			tx.commit();
		} finally {
			if (tx.isActive()) tx.rollback();
			em.close();
		}
	}
	
	@Override
	protected void onSetUp() {
		Map<String, String> properties = new HashMap<>();
		properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
		properties.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
		properties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
		properties.put("jakarta.persistence.jdbc.password", postgres.getPassword());
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.hbm2ddl.auto", "create-drop");
		properties.put("hibernate.show_sql", "true");
		properties.put("hibernate.format_sql", "true");
		
		emf = EmfFactory.createEntityManagerFactory("sessionbuilder-test", properties);
		cleanDatabase();
		
		GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
		});
		
		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
				bind(TopicServiceInterface.class).to(TopicService.class);
				bind(StudySessionInterface.class).to(StudySessionService.class);
				bind(TopicViewCallback.class).toInstance(managerView);
				bind(SessionViewCallback.class).toInstance(managerView);
				bind(TopicAndSessionManager.class).toInstance(managerView);
			}
		};
		
		Injector injector = Guice.createInjector(module);
		topicController = injector.getInstance(TopicController.class);
		sessionController = injector.getInstance(StudySessionController.class);
		injector.getInstance(TopicRepositoryInterface.class);
		injector.getInstance(StudySessionRepositoryInterface.class);
		transactionManager = injector.getInstance(TransactionManager.class);
		
		GuiActionRunner.execute(() -> {
			managerView.setTopicController(topicController);
			managerView.setSessionController(sessionController);
		});
		
		window = new FrameFixture(robot(), managerView);
		window.show();
	}
	
	@Override
	protected void onTearDown() throws Exception {
	    if (window != null) {
	        window.cleanUp();
	    }
	    if (emf != null && emf.isOpen()) {
	        emf.close();
	    }
	    super.onTearDown();
	}

	@Test
	public void testCreateTopicViaControllerIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			Topic t = topicController.handleCreateTopic("Matematica", "Algebra", 3, new ArrayList<>());
			assertThat(t).isNotNull();
			assertThat(t.getId()).isPositive();
			return t;
		});
		DefaultListModel<Topic> topicModel = managerView.getTopicModel();
		assertThat(topicModel.getSize()).isEqualTo(1);
		assertThat(topicModel.getElementAt(0).getName()).isEqualTo("Matematica");
		String[] topicContents = window.list("topicList").contents();
		assertThat(topicContents).hasSize(1);
		assertThat(topicContents[0]).contains("Matematica");
		Topic persistedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topic.getId()));
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Matematica");
		assertThat(persistedTopic.getDescription()).isEqualTo("Algebra");
		assertThat(persistedTopic.getDifficulty()).isEqualTo(3);
	}

	@Test
	public void testCreateSessionViaControllerIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Fisica", "Meccanica", 4, new ArrayList<>());
		});
		StudySession session = GuiActionRunner.execute(() -> {
			StudySession s = sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 90, "Sessione fisica", new ArrayList<>(List.of(topic.getId())));
			assertThat(s).isNotNull();
			assertThat(s.getId()).isPositive();
			return s;
		});
		DefaultListModel<StudySession> sessionModel = managerView.getStudySessionModel();
		assertThat(sessionModel.getSize()).isEqualTo(1);
		assertThat(sessionModel.getElementAt(0).getNote()).isEqualTo("Sessione fisica");
		String[] sessionContents = window.list("sessionList").contents();
		assertThat(sessionContents).hasSize(1);
		assertThat(sessionContents[0]).contains("Sessione fisica");
		StudySession persistedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(session.getId()));
		assertThat(persistedSession).isNotNull();
		assertThat(persistedSession.getNote()).isEqualTo("Sessione fisica");
		assertThat(persistedSession.getDuration()).isEqualTo(90);
		Topic finalTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topic.getId()));
		assertThat(persistedSession.getTopicList()).contains(finalTopic);
	}

	@Test
	public void testDeleteTopicButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Storia", "Rinascimento", 2, new ArrayList<>());
		});
		long topicId = topic.getId();
		Topic persistedBeforeDelete = transactionManager.doInTopicTransaction(repo -> repo.findById(topicId));
		assertThat(persistedBeforeDelete).isNotNull();
		window.list("topicList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withName("deleteTopicButton"));
		deleteButton.requireEnabled();
		deleteButton.click();
		assertThat(managerView.getTopicModel().getSize()).isZero();
		window.list("topicList").requireItemCount(0);
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText(" ");
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> transactionManager.doInTopicTransaction(repo -> repo.findById(topicId)));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}

	@Test
	public void testDeleteSessionButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Chimica", "Organica", 5, new ArrayList<>());
		});
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 120, "Sessione chimica", new ArrayList<>(List.of(topic.getId())));
		});
		long sessionId = session.getId();
		StudySession persistedBeforeDelete = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(persistedBeforeDelete).isNotNull();
		window.list("sessionList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withName("deleteSessionButton"));
		deleteButton.requireEnabled();
		deleteButton.click();
		assertThat(managerView.getStudySessionModel().getSize()).isZero();
		window.list("sessionList").requireItemCount(0);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId)));
		assertThat(exception.getMessage()).contains("non esiste una session con tale id");
	}

	@Test
	public void testCompleteSessionButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Biologia", "Genetica", 3, new ArrayList<>());
		});
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 60, "Sessione genetica", new ArrayList<>(List.of(topic.getId())));
		});
		assertThat(session.isComplete()).isFalse();
		StudySession persistedBeforeComplete = transactionManager.doInSessionTransaction(repo -> repo.findById(session.getId()));
		assertThat(persistedBeforeComplete.isComplete()).isFalse();
		window.list("sessionList").selectItem(0);
		JButtonFixture completeButton = window.button(JButtonMatcher.withName("completeSessionButton"));
		completeButton.requireEnabled();
		completeButton.click();
		StudySession updatedSession = GuiActionRunner.execute(() -> {
			return sessionController.handleGetSession(session.getId());
		});
		assertThat(updatedSession.isComplete()).isTrue();
		StudySession persistedAfterComplete = transactionManager.doInSessionTransaction(repo -> repo.findById(session.getId()));
		assertThat(persistedAfterComplete.isComplete()).isTrue();
	}

	@Test
	public void testTotalTimeButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Filosofia", "Etica", 3, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 60, "Sessione 1", new ArrayList<>(List.of(topic.getId())));
			sessionController.handleCreateSession(
				LocalDate.now().plusDays(2), 90, "Sessione 2", new ArrayList<>(List.of(topic.getId())));
		});
		Topic persistedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topic.getId()));
		assertThat(persistedTopic.getSessionList()).hasSize(2);
		assertThat(persistedTopic.totalTime()).isEqualTo(150);
		window.list("topicList").selectItem(0);
		JButtonFixture totalTimeButton = window.button(JButtonMatcher.withText("totalTime"));
		totalTimeButton.requireEnabled();
		totalTimeButton.click();
		window.label(JLabelMatcher.withName("errorMessageLabel"))
			.requireText("Tempo totale: 150 minuti");
	}

	@Test
	public void testPercentageButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Arte", "Pittura", 4, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionController.handleCreateSession(LocalDate.now().plusDays(1), 60, "Sessione non completata", new ArrayList<>(List.of(topic.getId())));
		});
		
		GuiActionRunner.execute(() -> {
			StudySession s = sessionController.handleCreateSession(LocalDate.now().plusDays(2), 90, "Sessione completata", new ArrayList<>(List.of(topic.getId())));
			sessionController.handleCompleteSession(s.getId());
			return s;
		});
		Topic persistedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topic.getId()));
		assertThat(persistedTopic.getSessionList()).hasSize(2);
		assertThat(persistedTopic.percentageOfCompletion()).isEqualTo(50);
		window.list("topicList").selectItem(0);
		JButtonFixture percentageButton = window.button(JButtonMatcher.withText("%Completion"));
		percentageButton.requireEnabled();
		percentageButton.click();
		window.label(JLabelMatcher.withName("errorMessageLabel"))
			.requireText("Percentuale di completamento: 50%");
	}

	@Test
	public void testButtonsEnabledStateWithRealDataIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Lingua", "Inglese", 2, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionController.handleCreateSession(LocalDate.now().plusDays(1), 45, "Lezione inglese", new ArrayList<>(List.of(topic.getId())));
		});
		window.list("topicList").selectItem(0);
		window.list("sessionList").selectItem(0);
		JButtonFixture deleteTopicButton = window.button(JButtonMatcher.withName("deleteTopicButton"));
		deleteTopicButton.requireEnabled();
		assertThat(deleteTopicButton.isEnabled()).isTrue();
		JButtonFixture deleteSessionButton = window.button(JButtonMatcher.withName("deleteSessionButton"));
		deleteSessionButton.requireEnabled();
		assertThat(deleteSessionButton.isEnabled()).isTrue();
		JButtonFixture completeSessionButton = window.button(JButtonMatcher.withName("completeSessionButton"));
		completeSessionButton.requireEnabled();
		assertThat(completeSessionButton.isEnabled()).isTrue();
		JButtonFixture totalTimeButton = window.button(JButtonMatcher.withText("totalTime"));
		totalTimeButton.requireEnabled();
		assertThat(totalTimeButton.isEnabled()).isTrue();
		JButtonFixture percentageButton = window.button(JButtonMatcher.withText("%Completion"));
		percentageButton.requireEnabled();
		assertThat(percentageButton.isEnabled()).isTrue();
		window.list("topicList").clearSelection();
		window.list("sessionList").clearSelection();
		deleteTopicButton.requireDisabled();
		assertThat(deleteTopicButton.isEnabled()).isFalse();
		deleteSessionButton.requireDisabled();
		assertThat(deleteSessionButton.isEnabled()).isFalse();
		completeSessionButton.requireDisabled();
		assertThat(completeSessionButton.isEnabled()).isFalse();
		totalTimeButton.requireDisabled();
		assertThat(totalTimeButton.isEnabled()).isFalse();
		percentageButton.requireDisabled();
		assertThat(percentageButton.isEnabled()).isFalse();
	}

	@Test
	public void testCompleteWorkflowIntegrationIt() {
		Topic topic1 = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Informatica", "Java", 5, new ArrayList<>());
		});
		Topic topic2 = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Database", "SQL", 4, new ArrayList<>());
		});
		StudySession session1 = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(LocalDate.now().plusDays(1), 120, "Java basics", new ArrayList<>(List.of(topic1.getId())));
		});
		StudySession session2 = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(LocalDate.now().plusDays(2), 90, "SQL queries", new ArrayList<>(List.of(topic2.getId())));
		});
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(2);
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(2);
		
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		assertThat(managerView.getStudySessionModel().getElementAt(0).isComplete()).isTrue();
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withText("totalTime")).click();
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText("Tempo totale: 120 minuti");
		window.button(JButtonMatcher.withText("%Completion")).click();
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText("Percentuale di completamento: 100%");
		window.list("topicList").selectItem(1);
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
		assertThat(managerView.getTopicModel().getElementAt(0).getName()).isEqualTo("Informatica");
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(2);
		assertThat(managerView.getStudySessionModel().getElementAt(0)).isEqualTo(session1);
		assertThat(managerView.getStudySessionModel().getElementAt(1)).isEqualTo(session2);
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(1);
		assertThat(managerView.getStudySessionModel().getElementAt(0)).isEqualTo(session2);
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		assertThat(managerView.getStudySessionModel().getSize()).isZero();
	}

	@Test
	public void testViewCallbackIntegrationIt() {
		GuiActionRunner.execute(() -> {
			assertThat(managerView.getTopicController()).isNotNull();
			assertThat(managerView.getSessionController()).isNotNull();
			assertThat(managerView.getTopicController().getViewCallback()).isEqualTo(managerView);
			assertThat(managerView.getSessionController().getViewCallback()).isEqualTo(managerView);
		});
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Test", "Test description", 1, new ArrayList<>());
		});
		assertThat(managerView.getTopicModel().contains(topic)).isTrue();
		Topic persistedTopic = transactionManager.doInTopicTransaction(repo -> repo.findById(topic.getId()));
		assertThat(persistedTopic.getName()).isEqualTo("Test");
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 30, "Test session", new ArrayList<>(List.of(topic.getId())));
		});
		assertThat(managerView.getStudySessionModel().contains(session)).isTrue();
		StudySession persistedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(session.getId()));
		assertThat(persistedSession.getNote()).isEqualTo("Test session");
	}
}