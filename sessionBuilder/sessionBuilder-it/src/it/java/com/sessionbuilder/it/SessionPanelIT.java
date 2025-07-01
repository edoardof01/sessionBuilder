package com.sessionbuilder.it;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.sessionbuilder.swing.SessionPanel;
import com.sessionbuilder.swing.TopicAndSessionManager;
import com.toedter.calendar.JDateChooser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@RunWith(GUITestRunner.class)
public class SessionPanelIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private SessionPanel sessionPanel;
	private EntityManagerFactory emf;
	private StudySessionController sessionController;
	private TopicController topicController;
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
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class);
				bind(TopicServiceInterface.class).to(TopicService.class);
				bind(StudySessionInterface.class).to(StudySessionService.class);
				bind(SessionViewCallback.class).toInstance(managerView); 
				bind(TopicViewCallback.class).toInstance(managerView);   
			}
		};

		Injector injector = Guice.createInjector(module);
		sessionController = injector.getInstance(StudySessionController.class);
		topicController = injector.getInstance(TopicController.class);
		injector.getInstance(StudySessionRepositoryInterface.class);

		GuiActionRunner.execute(() -> {
			managerView.setTopicController(topicController);
			managerView.setSessionController(sessionController); 
			
			sessionPanel = managerView.getSessionPanel(); 
			sessionPanel.setSessionController(sessionController); 
			managerView.showCreateSessionView(); 
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
	public void testAddSessionButtonEnabledWhenAllFieldsFilledIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Matematica", "Geometria", 3, new ArrayList<>());
		});
		
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.textBox("durationField").enterText("90");
		window.textBox("noteField").enterText("Sessione di geometria");
		window.list("sessionPanelTopicList").selectItem(getTopicString(topic)); 
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
	}

	@Test
	public void testAddSessionButtonDisabledWhenDateMissingIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Fisica", "Meccanica", 4, new ArrayList<>());
		});
		
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("Sessione di fisica");
		window.list("sessionPanelTopicList").selectItem(getTopicString(topic)); 
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
	}

	@Test
	public void testAddSessionButtonDisabledWhenTopicNotSelectedIt() {
		GuiActionRunner.execute(() -> {
			topicController.handleCreateTopic("Storia", "Medioevo", 2, new ArrayList<>());
		});
		
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.textBox("durationField").enterText("45");
		window.textBox("noteField").enterText("Sessione di storia");
		window.list("sessionPanelTopicList").clearSelection();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
	}

	@Test
	public void testCreateSessionSuccessIt() {
		Topic topic = GuiActionRunner.execute(() -> {
			return topicController.handleCreateTopic("Filosofia", "Etica", 3, new ArrayList<>());
		});
		
		setDateChooserValue(LocalDate.now().plusDays(2));
		window.textBox("durationField").enterText("120");
		window.textBox("noteField").enterText("Sessione di filosofia");
		window.list("sessionPanelTopicList").selectItem(getTopicString(topic)); 
		JButtonFixture addButton = window.button(JButtonMatcher.withName("addSessionButton"));
		addButton.requireEnabled();
		addButton.click();
		window.label(JLabelMatcher.withName("sessionErrorMessage")).requireText(""); 

		StudySession createdSession = GuiActionRunner.execute(() -> {
			DefaultListModel<StudySession> studySessionModel = managerView.getStudySessionModel(); 
			List<StudySession> sessions = new ArrayList<>();
			for (int i = 0; i < studySessionModel.getSize(); i++) {
				sessions.add(studySessionModel.getElementAt(i));
			}
			return sessions.isEmpty() ? null : sessions.get(sessions.size() - 1);
		});
		
		assertThat(createdSession).isNotNull();
		assertThat(createdSession.getDuration()).isEqualTo(120);
		assertThat(createdSession.getNote()).isEqualTo("Sessione di filosofia");
		assertThat(createdSession.getTopicList()).extracting(Topic::getId).contains(topic.getId());
	}

	@Test
	public void testShowSessionErrorIt() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "Test session", new ArrayList<>());
		GuiActionRunner.execute(() -> {
			sessionPanel.showSessionError("Errore di test", session);
		});
		String expectedText = "Errore di test: " + getStudySessionString(session);
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
		
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		window.textBox("durationField").enterText("90");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		window.textBox("noteField").enterText("Sessione sociologia");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		window.list("sessionPanelTopicList").selectItem(getTopicString(topic)); 
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
		window.textBox("durationField").deleteText();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
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

	private String getTopicString(Topic topic) {
		int sessionCount = topic.getSessionList() != null ? topic.getSessionList().size() : 0;
		return "Topic( name: " + topic.getName() + ", description: " + topic.getDescription() + 
		       ", difficulty: " + topic.getDifficulty() + ", numSessions: " + sessionCount +")";
	}

	private String getStudySessionString(StudySession session) {
	    String topicNames = session.getTopicList().stream()
	                               .map(Topic::getName)
	                               .collect(Collectors.joining(", "));
	    String completedStatus = session.isComplete() ? "Completed: true" : "Completed: false";
	    return "StudySession(" + session.getDate() + ", " + session.getDuration() + ", " + 
	           session.getNote() + ", " + completedStatus + ", topics{" + topicNames + "})";
	}
}