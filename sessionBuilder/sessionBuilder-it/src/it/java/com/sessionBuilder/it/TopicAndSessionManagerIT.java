package com.sessionBuilder.it;

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
import com.sessionbuilder.core.TransactionManager;
import com.sessionbuilder.core.TransactionManagerImpl;
import com.sessionbuilder.swing.TopicAndSessionManager;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class TopicAndSessionManagerIT extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	private TopicAndSessionManager managerView;
	private EntityManagerFactory emf;
	private TopicController topicController;
	private StudySessionController sessionController;
	private TopicRepositoryInterface topicRepository;
	private StudySessionRepositoryInterface sessionRepository;
	
	@SuppressWarnings("resource")
	@ClassRule
	public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName(System.getenv().getOrDefault("POSTGRES_TEST_DB", "test"))
			.withUsername(System.getenv().getOrDefault("POSTGRES_TEST_USER", "test"))
			.withPassword(System.getenv().getOrDefault("POSTGRES_TEST_PASSWORD", "test"));
	
	@BeforeClass
	public static void setUpContainer() {
		postgres.start();
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
		
		emf = Persistence.createEntityManagerFactory("sessionbuilder-test", properties);
		
		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
				bind(TopicServiceInterface.class).to(TopicService.class);
				bind(StudySessionInterface.class).to(StudySessionService.class);
			}
		};
		
		Injector injector = Guice.createInjector(module);
		topicController = injector.getInstance(TopicController.class);
		sessionController = injector.getInstance(StudySessionController.class);
		topicRepository = injector.getInstance(TopicRepositoryInterface.class);
		sessionRepository = injector.getInstance(StudySessionRepositoryInterface.class);
		
		GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
			managerView.setTopicController(topicController);
			managerView.setSessionController(sessionController);
		});
		
		window = new FrameFixture(robot(), managerView);
		window.show();
	}
	
	@Override
	protected void onTearDown() throws Exception {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
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
		Topic persistedTopic = topicRepository.findById(topic.getId());
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
				LocalDate.now().plusDays(1), 90, "Sessione fisica", new ArrayList<>(List.of(topic)));
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
		StudySession persistedSession = sessionRepository.findById(session.getId());
		assertThat(persistedSession).isNotNull();
		assertThat(persistedSession.getNote()).isEqualTo("Sessione fisica");
		assertThat(persistedSession.getDuration()).isEqualTo(90);
		assertThat(persistedSession.getTopicList()).contains(topic);
	}

	@Test
	public void testDeleteTopicButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Storia", "Rinascimento", 2, new ArrayList<>());
		});
		long topicId = topic.getId();
		Topic persistedBeforeDelete = topicRepository.findById(topicId);
		assertThat(persistedBeforeDelete).isNotNull();
		window.list("topicList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withName("deleteTopicButton"));
		deleteButton.requireEnabled();
		deleteButton.click();
		assertThat(managerView.getTopicModel().getSize()).isZero();
		window.list("topicList").requireItemCount(0);
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText(" ");
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> topicRepository.findById(topicId));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
	}

	@Test
	public void testDeleteSessionButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Chimica", "Organica", 5, new ArrayList<>());
		});
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 120, "Sessione chimica", new ArrayList<>(List.of(topic)));
		});
		long sessionId = session.getId();
		StudySession persistedBeforeDelete = sessionRepository.findById(sessionId);
		assertThat(persistedBeforeDelete).isNotNull();
		window.list("sessionList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withName("deleteSessionButton"));
		deleteButton.requireEnabled();
		deleteButton.click();
		assertThat(managerView.getStudySessionModel().getSize()).isZero();
		window.list("sessionList").requireItemCount(0);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> sessionRepository.findById(sessionId));
		assertThat(exception.getMessage()).contains("la sessione cercata non esiste");
	}

	@Test
	public void testCompleteSessionButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Biologia", "Genetica", 3, new ArrayList<>());
		});
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 60, "Sessione genetica", new ArrayList<>(List.of(topic)));
		});
		assertThat(session.isComplete()).isFalse();
		StudySession persistedBeforeComplete = sessionRepository.findById(session.getId());
		assertThat(persistedBeforeComplete.isComplete()).isFalse();
		window.list("sessionList").selectItem(0);
		JButtonFixture completeButton = window.button(JButtonMatcher.withName("completeSessionButton"));
		completeButton.requireEnabled();
		completeButton.click();
		StudySession updatedSession = GuiActionRunner.execute(() -> {
			return sessionController.handleGetSession(session.getId());
		});
		assertThat(updatedSession.isComplete()).isTrue();
		StudySession persistedAfterComplete = sessionRepository.findById(session.getId());
		assertThat(persistedAfterComplete.isComplete()).isTrue();
	}

	@Test
	public void testTotalTimeButtonIntegrationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Filosofia", "Etica", 3, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 60, "Sessione 1", new ArrayList<>(List.of(topic)));
			sessionController.handleCreateSession(
				LocalDate.now().plusDays(2), 90, "Sessione 2", new ArrayList<>(List.of(topic)));
		});
		Topic persistedTopic = topicRepository.findById(topic.getId());
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
		StudySession session1 = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 60, "Sessione non completata", new ArrayList<>(List.of(topic)));
		});
		StudySession session2 = GuiActionRunner.execute(() -> {
			StudySession s = sessionController.handleCreateSession(
				LocalDate.now().plusDays(2), 90, "Sessione completata", new ArrayList<>(List.of(topic)));
			sessionController.handleCompleteSession(s.getId());
			return s;
		});
		assertThat(sessionRepository.findById(session1.getId())).isNotNull();
		assertThat(sessionRepository.findById(session2.getId())).isNotNull();
		Topic persistedTopic = topicRepository.findById(topic.getId());
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
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 45, "Lezione inglese", new ArrayList<>(List.of(topic)));
		});
		assertThat(topicRepository.findById(topic.getId())).isNotNull();
		assertThat(sessionRepository.findById(session.getId())).isNotNull();
		window.list("topicList").selectItem(0);
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteTopicButton")).requireEnabled();
		window.button(JButtonMatcher.withName("deleteSessionButton")).requireEnabled();
		window.button(JButtonMatcher.withName("completeSessionButton")).requireEnabled();
		window.button(JButtonMatcher.withText("totalTime")).requireEnabled();
		window.button(JButtonMatcher.withText("%Completion")).requireEnabled();
		window.list("topicList").clearSelection();
		window.list("sessionList").clearSelection();
		window.button(JButtonMatcher.withName("deleteTopicButton")).requireDisabled();
		window.button(JButtonMatcher.withName("deleteSessionButton")).requireDisabled();
		window.button(JButtonMatcher.withName("completeSessionButton")).requireDisabled();
		window.button(JButtonMatcher.withText("totalTime")).requireDisabled();
		window.button(JButtonMatcher.withText("%Completion")).requireDisabled();
	}

	@Test
	public void testCompleteWorkflowIntegrationIt() {
		Topic topic1 = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Informatica", "Java", 5, new ArrayList<>());
		});
		Topic topic2 = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Database", "SQL", 4, new ArrayList<>());
		});
		long topic2Id = topic2.getId();
		StudySession session1 = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 120, "Java basics", new ArrayList<>(List.of(topic1)));
		});
		long session1Id = session1.getId();
		StudySession session2 = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(2), 90, "SQL queries", new ArrayList<>(List.of(topic2)));
		});
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(2);
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(2);
		assertThat(topicRepository.findById(topic1.getId())).isNotNull();
		assertThat(topicRepository.findById(topic2.getId())).isNotNull();
		assertThat(sessionRepository.findById(session1.getId())).isNotNull();
		assertThat(sessionRepository.findById(session2.getId())).isNotNull();
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		StudySession completedSession = sessionRepository.findById(session1.getId());
		assertThat(completedSession.isComplete()).isTrue();
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withText("totalTime")).click();
		window.label(JLabelMatcher.withName("errorMessageLabel"))
			.requireText("Tempo totale: 120 minuti");
		window.button(JButtonMatcher.withText("%Completion")).click();
		window.label(JLabelMatcher.withName("errorMessageLabel"))
			.requireText("Percentuale di completamento: 100%");
		window.list("topicList").selectItem(1);
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
		assertThat(managerView.getTopicModel().getElementAt(0).getName()).isEqualTo("Informatica");
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
			() -> topicRepository.findById(topic2Id));
		assertThat(exception.getMessage()).isEqualTo("non esiste un topic con tale id");
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		assertThat(managerView.getStudySessionModel().getSize()).isZero();
		IllegalArgumentException sessionException = assertThrows(IllegalArgumentException.class, 
			() -> sessionRepository.findById(session1Id));
		assertThat(sessionException.getMessage()).contains("la sessione cercata non esiste");
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
		Topic persistedTopic = topicRepository.findById(topic.getId());
		assertThat(persistedTopic.getName()).isEqualTo("Test");
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 30, "Test session", new ArrayList<>(List.of(topic)));
		});
		assertThat(managerView.getStudySessionModel().contains(session)).isTrue();
		StudySession persistedSession = sessionRepository.findById(session.getId());
		assertThat(persistedSession.getNote()).isEqualTo("Test session");
	}
}