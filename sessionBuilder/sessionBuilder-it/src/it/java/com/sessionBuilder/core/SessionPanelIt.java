package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import org.assertj.swing.core.ComponentMatcher;
import org.assertj.swing.core.GenericTypeMatcher;
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
import com.sessionBuilder.swing.SessionPanel;
import com.sessionBuilder.swing.TopicAndSessionManager;
import com.toedter.calendar.JDateChooser;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class SessionPanelIt extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private SessionPanel sessionPanel;
	private EntityManagerFactory emf;
	private StudySessionController sessionController;
	private TopicController topicController;
	private StudySessionRepositoryInterface sessionRepository;
	private TopicRepositoryInterface topicRepository;
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
		sessionController = injector.getInstance(StudySessionController.class);
		topicController = injector.getInstance(TopicController.class);
		topicRepository = injector.getInstance(TopicRepositoryInterface.class);
		sessionRepository = injector.getInstance(StudySessionRepositoryInterface.class);

		GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
			DefaultListModel<Topic> topicModelForSessionPanel = new DefaultListModel<>();
			sessionPanel = new SessionPanel(topicModelForSessionPanel);
			sessionPanel.setSessionController(sessionController);
			sessionPanel.setManagerView(managerView);
			sessionController.setViewCallBack(managerView);
			JFrame frame = new JFrame();
			frame.add(sessionPanel);
			return frame;
		});

		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			JFrame frame = new JFrame();
			frame.add(sessionPanel);
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
	public void testAddSessionButtonEnabledWhenAllFieldsFilledIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Matematica", "Geometria", 3, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionPanel.getTopicModel().addElement(topic);
		});
		Topic persistedTopic = topicRepository.findById(topic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Matematica");
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.textBox("durationField").enterText("90");
		window.textBox("noteField").enterText("Sessione di geometria");
		window.list("sessionPanelTopicList").selectItem(0);
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
	}

	@Test
	public void testAddSessionButtonDisabledWhenDateMissingIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Fisica", "Meccanica", 4, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionPanel.getTopicModel().addElement(topic);
		});
		Topic persistedTopic = topicRepository.findById(topic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Fisica");
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("Sessione di fisica");
		window.list("sessionPanelTopicList").selectItem(0);
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
	}

	@Test
	public void testAddSessionButtonDisabledWhenTopicNotSelectedIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Storia", "Medioevo", 2, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionPanel.getTopicModel().addElement(topic);
		});
		Topic persistedTopic = topicRepository.findById(topic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Storia");
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.textBox("durationField").enterText("45");
		window.textBox("noteField").enterText("Sessione di storia");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
	}

	@Test
	public void testCreateSessionSuccessIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Filosofia", "Etica", 3, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionPanel.getTopicModel().addElement(topic);
		});
		Topic persistedTopic = topicRepository.findById(topic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Filosofia");
		setDateChooserValue(LocalDate.now().plusDays(2));
		window.textBox("durationField").enterText("120");
		window.textBox("noteField").enterText("Sessione di filosofia");
		window.list("sessionPanelTopicList").selectItem(0);
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addSessionButton"));
		addButton.requireEnabled();
		addButton.click();
		window.label(JLabelMatcher.withName("sessionErrorMessage")).requireText("");
		StudySession createdSession = GuiActionRunner.execute(() -> {
			List<StudySession> sessions = new ArrayList<>();
			for (int i = 0; i < managerView.getStudySessionModel().getSize(); i++) {
				sessions.add(managerView.getStudySessionModel().getElementAt(i));
			}
			return sessions.isEmpty() ? null : sessions.get(sessions.size() - 1);
		});
		assertThat(createdSession).isNotNull();
		assertThat(createdSession.getDuration()).isEqualTo(120);
		assertThat(createdSession.getNote()).isEqualTo("Sessione di filosofia");
		assertThat(createdSession.getTopicList()).contains(topic);
		StudySession persistedSession = sessionRepository.findById(createdSession.getId());
		assertThat(persistedSession).isNotNull();
		assertThat(persistedSession.getDuration()).isEqualTo(120);
		assertThat(persistedSession.getNote()).isEqualTo("Sessione di filosofia");
	}

	@Test
	public void testCreateSessionWithMultipleTopicsIt() {
		Topic topic1 = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Informatica", "Java", 5, new ArrayList<>());
		});
		Topic topic2 = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Database", "SQL", 4, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionPanel.getTopicModel().addElement(topic1);
			sessionPanel.getTopicModel().addElement(topic2);
		});
		Topic persistedTopic1 = topicRepository.findById(topic1.getId());
		Topic persistedTopic2 = topicRepository.findById(topic2.getId());
		assertThat(persistedTopic1).isNotNull();
		assertThat(persistedTopic2).isNotNull();
		assertThat(persistedTopic1.getName()).isEqualTo("Informatica");
		assertThat(persistedTopic2.getName()).isEqualTo("Database");
		setDateChooserValue(LocalDate.now().plusDays(3));
		window.textBox("durationField").enterText("180");
		window.textBox("noteField").enterText("Sessione multidisciplinare");
		window.list("sessionPanelTopicList").selectItems(0, 1);
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addSessionButton"));
		addButton.requireEnabled();
		addButton.click();
		window.label(JLabelMatcher.withName("sessionErrorMessage")).requireText("");
		StudySession createdSession = GuiActionRunner.execute(() -> {
			List<StudySession> sessions = new ArrayList<>();
			for (int i = 0; i < managerView.getStudySessionModel().getSize(); i++) {
				sessions.add(managerView.getStudySessionModel().getElementAt(i));
			}
			return sessions.isEmpty() ? null : sessions.get(sessions.size() - 1);
		});
		assertThat(createdSession).isNotNull();
		assertThat(createdSession.getTopicList()).hasSize(2);
		assertThat(createdSession.getTopicList()).containsExactlyInAnyOrder(topic1, topic2);
		StudySession persistedSession = sessionRepository.findById(createdSession.getId());
		assertThat(persistedSession.getTopicList()).hasSize(2);
	}

	@Test
	public void testShowSessionErrorIt() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "Test session", new ArrayList<>());
		GuiActionRunner.execute(() -> {
			sessionPanel.showSessionError("Errore di test", session);
		});
		String expectedText = "Errore di test: " + session.toString();
		window.label(JLabelMatcher.withName("sessionErrorMessage")).requireText(expectedText);
		assertThat(window.label(JLabelMatcher.withName("sessionErrorMessage")).text()).isEqualTo(expectedText);
	}

	@Test
	public void testShowGeneralErrorIt() {
		GuiActionRunner.execute(() -> {
			sessionPanel.showGeneralError("Errore generale di test");
		});
		window.label(JLabelMatcher.withName("sessionErrorMessage")).requireText("Errore generale di test");
		assertThat(window.label(JLabelMatcher.withName("sessionErrorMessage")).text()).isEqualTo("Errore generale di test");
	}

	@Test
	public void testButtonStateChangesWithFieldModificationIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Sociologia", "Teorie", 3, new ArrayList<>());
		});
		GuiActionRunner.execute(() -> {
			sessionPanel.getTopicModel().addElement(topic);
		});
		Topic persistedTopic = topicRepository.findById(topic.getId());
		assertThat(persistedTopic).isNotNull();
		assertThat(persistedTopic.getName()).isEqualTo("Sociologia");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		window.textBox("durationField").enterText("90");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		window.textBox("noteField").enterText("Sessione sociologia");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		window.list("sessionPanelTopicList").selectItem(0);
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		window.textBox("durationField").deleteText();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
	}

	private void setDateChooserValue(LocalDate localDate) {
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			Date javaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(javaDate);
		});
		robot().waitForIdle();
	}
}