package com.sessionbuilder.it;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

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
import com.google.inject.Singleton;
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
import com.sessionbuilder.swing.TopicPanel;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@RunWith(GUITestRunner.class)
public class TopicPanelIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private TopicPanel topicPanel;
	private EntityManagerFactory emf;
	private TopicController topicController;
	private StudySessionController sessionController;
	private TopicAndSessionManager managerView;

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

		JFrame mainFrame = GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
			return managerView;
		});

		AbstractModule module = new AbstractModule() {
			@Override
			protected void configure() {
				bind(EntityManagerFactory.class).toInstance(emf);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
				bind(StudySessionInterface.class).to(StudySessionService.class).in(Singleton.class);
				bind(TopicViewCallback.class).toInstance(managerView);
				bind(SessionViewCallback.class).toInstance(managerView);
			}
		};

		Injector injector = Guice.createInjector(module);
		topicController = injector.getInstance(TopicController.class);
		sessionController = injector.getInstance(StudySessionController.class);

		GuiActionRunner.execute(() -> {
			managerView.setTopicController(topicController);
			managerView.setSessionController(sessionController);

			topicPanel = managerView.getTopicPanel();

			topicPanel.setTopicController(topicController);
			
			topicPanel.setManagerView(managerView); 

			managerView.showCreateTopicView();
		});

		window = new FrameFixture(robot(), mainFrame);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
		if (window != null) {
			window.cleanUp();
		}
	}

	@Test
	public void testCreateTopicSuccessIt() {
		window.textBox("nameField").enterText("Fisica");
		window.textBox("descriptionField").enterText("Meccanica quantistica");
		window.textBox("difficultyField").enterText("5");
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addTopicButton"));
		addButton.requireEnabled();
		addButton.click();
		window.label(JLabelMatcher.withName("errorTopicPanelLbl")).requireText(" ");
		Topic createdTopic = GuiActionRunner.execute(() -> {
			List<Topic> topics = new ArrayList<>();
			for (int i = 0; i < managerView.getTopicModel().getSize(); i++) {
				topics.add(managerView.getTopicModel().getElementAt(i));
			}
			return topics.isEmpty() ? null : topics.get(topics.size() - 1);
		});
		assertThat(createdTopic).isNotNull();
		assertThat(createdTopic.getName()).isEqualTo("Fisica");
		assertThat(createdTopic.getDescription()).isEqualTo("Meccanica quantistica");
		assertThat(createdTopic.getDifficulty()).isEqualTo(5);
	}

	@Test
	public void testCreateTopicWithSessionsIt() {
		StudySession session = GuiActionRunner.execute(() -> {
			return sessionController.handleCreateSession(
				LocalDate.now().plusDays(1), 90, "Sessione test", new ArrayList<>());
		});
		
		GuiActionRunner.execute(() -> {
			topicPanel.getSessionModel().addElement(session);
		});
		
		window.textBox("nameField").enterText("Chimica");
		window.textBox("descriptionField").enterText("Chimica organica");
		window.textBox("difficultyField").enterText("4");
		
		robot().waitForIdle(); 
		window.list("topicPanelSessionList").selectItem(0); 
		
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addTopicButton"));
		addButton.requireEnabled();
		addButton.click();
		
		robot().waitForIdle(); 
		window.label(JLabelMatcher.withName("errorTopicPanelLbl")).requireText(" ");
		
		Topic createdTopic = GuiActionRunner.execute(() -> {
			List<Topic> topics = new ArrayList<>();
			for (int i = 0; i < managerView.getTopicModel().getSize(); i++) {
				topics.add(managerView.getTopicModel().getElementAt(i));
			}
			return topics.isEmpty() ? null : topics.get(topics.size() - 1);
		});
		assertThat(createdTopic).isNotNull();
		assertThat(createdTopic.getName()).isEqualTo("Chimica");
		assertThat(createdTopic.getSessionList()).contains(session);
	}

	@Test
	public void testCreateTopicWithInvalidDifficultyIt() {
		window.textBox("nameField").enterText("Storia");
		window.textBox("descriptionField").enterText("Storia medievale");
		window.textBox("difficultyField").enterText("10");
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addTopicButton"));
		addButton.requireEnabled();
		addButton.click();
		robot().waitForIdle(); 
		String errorText = window.label(JLabelMatcher.withName("errorTopicPanelLbl")).text();
		assertThat(errorText).contains("Errore nel salvare il topic:");
	}

	@Test
	public void testShowTopicErrorIt() {
		Topic topic = new Topic("Test Topic", "Test Description", 1, new ArrayList<>());
		GuiActionRunner.execute(() -> {
			topicPanel.showTopicError("Errore di test", topic);
		});
		String expectedText = "Errore di test: " + getTopicString(topic, 0);
		window.label(JLabelMatcher.withName("errorTopicPanelLbl")).requireText(expectedText);
		assertThat(window.label(JLabelMatcher.withName("errorTopicPanelLbl")).text()).isEqualTo(expectedText);
	}

	@Test
	public void testShowGeneralErrorIt() {
		GuiActionRunner.execute(() -> {
			topicPanel.showGeneralError("Errore generale di test");
		});
		window.label(JLabelMatcher.withName("errorTopicPanelLbl")).requireText("Errore generale di test");
		assertThat(window.label(JLabelMatcher.withName("errorTopicPanelLbl")).text()).isEqualTo("Errore generale di test");
	}

	@Test
	public void testButtonStateChangesWithFieldModificationIt() {
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();
		window.textBox("nameField").enterText("Test");
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		window.textBox("descriptionField").enterText("Descrizione");
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		window.textBox("difficultyField").enterText("2");
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isTrue();
		window.button(JButtonMatcher.withName("addTopicButton")).requireEnabled();
		window.textBox("nameField").deleteText();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
	}

	private String getTopicString(Topic topic, int sessionCount) {
		return "Topic( name: " + topic.getName() + ", description: " + topic.getDescription() + 
		       ", difficulty: " + topic.getDifficulty() + ", numSessions: " + sessionCount + ")";
	}
}