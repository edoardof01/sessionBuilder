package com.sessionBuilder.swing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sessionBuilder.core.StudySession;
import com.sessionBuilder.core.Topic;
import com.sessionBuilder.core.TopicController;


@RunWith(GUITestRunner.class)
public class TopicPanelTest extends AssertJSwingJUnitTestCase{
	
	private FrameFixture window;
	private TopicAndSessionManager managerView;
	
	private String name;
	private String description;
	private int difficulty;
	private Topic topic;
	
	private LocalDate date;
	private int duration;
	private String note;
	private StudySession session;
	
	
	@Mock
	private TopicController topicController;
	
	private AutoCloseable closeable;
	
	@Override
	protected void onSetUp() throws Exception {
		name = "giardinaggio";
		description = "fiori";
		difficulty = 3;
		closeable = MockitoAnnotations.openMocks(this);
		topic = new Topic(name, description, difficulty, new ArrayList<>());
		date = LocalDate.now();
		duration = 60;
		note = "una nota";
		session = new StudySession(date, duration, note, new ArrayList<>(List.of(topic)));
		GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
		});
		window = new FrameFixture(robot(), managerView);
		window.show();
		managerView.getTopicPanel().setManagerView(managerView);
		GuiActionRunner.execute(() -> {
			managerView.showCreateTopicView();
		});
		
	}
	
	@Override
	public void onTearDown() throws Exception {
		window.cleanUp();
		closeable.close();
	}
	
	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Name:"));
		window.label(JLabelMatcher.withText("Description:"));
		window.label(JLabelMatcher.withText("Difficulty:"));
		window.label(JLabelMatcher.withText("Sessions:"));
		window.textBox("nameField").requireEnabled();
		window.textBox("descriptionField").requireEnabled();
		window.textBox("difficultyField").requireEnabled();
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		assertThat(window.textBox("nameField").isEnabled()).isTrue();
		assertThat(window.textBox("descriptionField").isEnabled()).isTrue();
		assertThat(window.textBox("difficultyField").isEnabled()).isTrue();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();
	}

	@Test @GUITest
	public void testWhenFieldsAreNotEmptyAddButtonIsEnabled() {
		window.textBox("nameField").enterText(name);
		window.textBox("difficultyField").enterText(String.valueOf(difficulty));
		window.textBox("descriptionField").enterText(description);
		window.button(JButtonMatcher.withName("addTopicButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isTrue();
		assertThat(window.textBox("nameField").text()).isEqualTo(name);
		assertThat(window.textBox("descriptionField").text()).isEqualTo(description);
	}

	@Test @GUITest
	public void testWhenEitherNameOrDescriptionOrDifficultyAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture nameText = window.textBox("nameField");
		JTextComponentFixture descriptionText = window.textBox("descriptionField");
		JTextComponentFixture difficultyText = window.textBox("difficultyField");

		nameText.enterText(name);
		descriptionText.enterText(description);
		difficultyText.enterText("");
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();

		nameText.deleteText();
		nameText.enterText("");
		difficultyText.enterText(String.valueOf(difficulty));
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();

		nameText.enterText(name);
		descriptionText.deleteText();
		descriptionText.enterText("");
		window.button(JButtonMatcher.withName("addTopicButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addTopicButton")).isEnabled()).isFalse();
	}

	@Test @GUITest
	public void testBackButtonWithNullManagerViewDoesNothing() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicPanel().setManagerView(null);
		});
		window.button(JButtonMatcher.withName("backButton")).click();
		robot().waitForIdle();
		assertThat(window.button(JButtonMatcher.withName("backButton")).isEnabled()).isTrue();
	}
	
	@Test @GUITest
	public void testBackButtonWithManagerViewCallsShowMainView() {
		window.button(JButtonMatcher.withName("backButton")).click();
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			assertThat(managerView.isDisplayable()).isTrue();
		});
	}
	
	@Test @GUITest
	public void testTopicShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showCreateTopicView();
			managerView.getTopicPanel().showTopicError("error message", topic);
		});
		window.label("errorTopicPanelLbl").requireText("error message: "+ topic);
		assertThat(window.label("errorTopicPanelLbl").text()).isEqualTo("error message: "+ topic);
	}

	@Test
	@GUITest
	public void testAddButtonEnabledifClickedShouldResetErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicPanel().showGeneralError("error");
		});
		window.textBox("nameField").enterText("test");
		window.textBox("descriptionField").enterText("descrizione");
		window.textBox("difficultyField").enterText("1");
		managerView.getTopicPanel().setTopicController(topicController);
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		window.label("errorTopicPanelLbl").requireText(" ");
		assertThat(window.label("errorTopicPanelLbl").text()).isEqualTo(" ");
	}
	
	@Test @GUITest
	public void testAddButtonWithoutSelectedSessionsCallsTopicControllerCreateTopic() {
		JTextComponentFixture nameField = window.textBox("nameField").enterText("test");
		JTextComponentFixture descriptionField = window.textBox("descriptionField").enterText("descrizione");
		JTextComponentFixture difficultyField = window.textBox("difficultyField").enterText("1");
		managerView.getTopicPanel().setTopicController(topicController);
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		verify(topicController).handleCreateTopic(nameField.text(), descriptionField.text(), Integer.parseInt(difficultyField.text()), new ArrayList<>());
		robot().waitForIdle();
	}
	
	@Test @GUITest
	public void testAddButtonWithSelectedSessionsCallsTopicControllerCreateTopic() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicPanel().getSessionModel().addElement(session);
			managerView.getTopicPanel().setTopicController(topicController);
			managerView.getStudySessionModel().addElement(session);
		});
		JTextComponentFixture nameField = window.textBox("nameField").enterText("test");
		JTextComponentFixture descriptionField = window.textBox("descriptionField").enterText("descrizione");
		JTextComponentFixture difficultyField = window.textBox("difficultyField").enterText("1");
		window.list("topicPanelSessionList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		verify(topicController).handleCreateTopic(nameField.text(), descriptionField.text(), Integer.parseInt(difficultyField.text()), new ArrayList<>(List.of(session)));
		robot().waitForIdle();
		List<Topic> topics = Collections.list(managerView.getTopicModel().elements());
		Topic topic = topics.get(0);
		assertThat(topic.getName()).isEqualTo(nameField.text());
		assertThat(topic.getDescription()).isEqualTo(descriptionField.text());
		assertThat(topic.getDifficulty()).isEqualTo(Integer.parseInt(difficultyField.text()));
		assertThat(topic.getSessionList()).containsExactly(session);
	}
	
	@Test @GUITest
	public void testAddButtonWithoutAControllerSelectedDoNothing() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicPanel().getSessionModel().addElement(session);
			managerView.getStudySessionModel().addElement(session);
		});
		JTextComponentFixture nameField = window.textBox("nameField").enterText("test");
		JTextComponentFixture descriptionField = window.textBox("descriptionField").enterText("descrizione");
		JTextComponentFixture difficultyField = window.textBox("difficultyField").enterText("1");
		window.list("topicPanelSessionList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		verify(topicController, times(0)).handleCreateTopic(nameField.text(), descriptionField.text(), Integer.parseInt(difficultyField.text()), new ArrayList<>(List.of(session)));
		List<Topic> topics = Collections.list(managerView.getTopicModel().elements());
		assertThat(topics).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@GUITest
	public void testAddButtonWithAnErrorThrowException() {
		doThrow(new RuntimeException("bug del backend")).when(topicController).handleCreateTopic(anyString(), anyString(), anyInt(), any(ArrayList.class));
		GuiActionRunner.execute(() -> {
			managerView.getTopicPanel().getSessionModel().addElement(session);
			managerView.getTopicPanel().setTopicController(topicController);
			managerView.getStudySessionModel().addElement(session);
		});
		window.textBox("nameField").enterText("test");
		window.textBox("descriptionField").enterText("descrizione");
		window.textBox("difficultyField").enterText("1");
		window.list("topicPanelSessionList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		assertThat(window.label("errorTopicPanelLbl").text()).contains("Errore nel salvare il topic: bug del backend");
	}
	
	
	
	
	
	



}
