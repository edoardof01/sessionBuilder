package com.sessionbuilder.swing.it;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import com.sessionbuilder.swing.TopicPanel;

public class TopicPanelIT extends BaseFrontendIntegrationTest {

	private TopicPanel topicPanel;
	private TopicController topicController;
	private StudySessionController sessionController;

	@Override
	protected void onSetUp() {
		super.onSetUp();

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
	}

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(TopicServiceInterface.class).to(TopicService.class).in(Singleton.class);
				bind(StudySessionInterface.class).to(StudySessionService.class).in(Singleton.class);
			}
		};
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