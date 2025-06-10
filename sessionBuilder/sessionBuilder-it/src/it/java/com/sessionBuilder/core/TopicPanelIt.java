package com.sessionBuilder.core;

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
import com.google.inject.Module;
import com.sessionBuilder.swing.TopicAndSessionManager;
import com.sessionBuilder.swing.TopicPanel;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class TopicPanelIt extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private TopicPanel topicPanel;
	private EntityManagerFactory emf;
	private TopicController topicController;
	private StudySessionController sessionController;
	private TopicRepositoryInterface topicRepository;
	private StudySessionRepositoryInterface sessionRepository;
	private TopicAndSessionManager managerView;

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

		Module module = new AbstractModule() {
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
			topicPanel = new TopicPanel();
			topicPanel.setTopicController(topicController);
			topicPanel.setManagerView(managerView);
			JFrame frame = new JFrame();
			frame.add(topicPanel);
			return frame;
		});

		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			JFrame frame = new JFrame();
			frame.add(topicPanel);
			return frame;
		}));
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		if (emf != null && emf.isOpen()) {
			emf.close();
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
		Topic persistedTopic = topicRepository.findById(createdTopic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Fisica");
		assertThat(persistedTopic.getDescription()).isEqualTo("Meccanica quantistica");
		assertThat(persistedTopic.getDifficulty()).isEqualTo(5);
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
		StudySession persistedSession = sessionRepository.findById(session.getId());
		assertThat(persistedSession).isNotNull();
		assertThat(persistedSession.getNote()).isEqualTo("Sessione test");
		window.textBox("nameField").enterText("Chimica");
		window.textBox("descriptionField").enterText("Chimica organica");
		window.textBox("difficultyField").enterText("4");
		window.list("topicPanelSessionList").selectItem(0);
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
		assertThat(createdTopic.getName()).isEqualTo("Chimica");
		assertThat(createdTopic.getSessionList()).contains(session);
		Topic persistedTopic = topicRepository.findById(createdTopic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getSessionList()).contains(session);
	}

	@Test
	public void testCreateTopicWithInvalidDifficultyIt() {
		window.textBox("nameField").enterText("Storia");
		window.textBox("descriptionField").enterText("Storia medievale");
		window.textBox("difficultyField").enterText("10");
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addTopicButton"));
		addButton.requireEnabled();
		addButton.click();
		String errorText = window.label(JLabelMatcher.withName("errorTopicPanelLbl")).text();
		assertThat(errorText).contains("Errore nel salvare il topic:");
	}

	@Test
	public void testShowTopicErrorIt() {
		Topic topic = new Topic("Test Topic", "Test Description", 1, new ArrayList<>());
		GuiActionRunner.execute(() -> {
			topicPanel.showTopicError("Errore di test", topic);
		});
		String expectedText = "Errore di test: " + topic.toString();
		window.label(JLabelMatcher.withName("errorTopicPanelLbl")).requireText(expectedText);
	}

	@Test
	public void testShowGeneralErrorIt() {
		GuiActionRunner.execute(() -> {
			topicPanel.showGeneralError("Errore generale di test");
		});
		window.label(JLabelMatcher.withName("errorTopicPanelLbl")).requireText("Errore generale di test");
	}

	@Test
	public void testButtonStateChangesWithFieldModificationIt() {
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		window.textBox("nameField").enterText("Test");
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		window.textBox("descriptionField").enterText("Descrizione");
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		window.textBox("difficultyField").enterText("2");
		window.button(JButtonMatcher.withName("addTopicButton")).requireEnabled();
		window.textBox("nameField").deleteText();
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
	}
}