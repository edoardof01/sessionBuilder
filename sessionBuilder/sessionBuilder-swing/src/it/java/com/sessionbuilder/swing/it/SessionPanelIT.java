package com.sessionbuilder.swing.it;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;

import org.assertj.swing.core.ComponentMatcher;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sessionbuilder.core.backend.StudySession;
import com.sessionbuilder.core.backend.StudySessionController;
import com.sessionbuilder.core.backend.StudySessionInterface;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.StudySessionService;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicController;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TopicService;
import com.sessionbuilder.core.backend.TopicServiceInterface;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;
import com.sessionbuilder.swing.SessionPanel;
import com.toedter.calendar.JDateChooser;

public class SessionPanelIT extends BaseFrontendIntegrationTest {

	private SessionPanel sessionPanel;
	private StudySessionController sessionController;
	private TopicController topicController;

	@Override
	protected void onSetUp() {
		super.onSetUp();

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
	}

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(TopicRepositoryInterface.class).to(TopicRepository.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
				bind(StudySessionInterface.class).to(StudySessionService.class).in(Singleton.class);
			}
		};
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