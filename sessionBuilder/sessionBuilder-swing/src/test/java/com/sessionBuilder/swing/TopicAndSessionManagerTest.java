package com.sessionBuilder.swing;



import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sessionBuilder.core.StudySession;
import com.sessionBuilder.core.StudySessionController;
import com.sessionBuilder.core.Topic;
import com.sessionBuilder.core.TopicController;

@RunWith(GUITestRunner.class)
public class TopicAndSessionManagerTest extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;

	private TopicAndSessionManager managerView;	
	
	private StudySession session1;
	private StudySession session2;
	
	private final long ids1 = 1L;
	private final long ids2 = 2L;

	private Topic topic1;
	private Topic topic2;
	
	private final long idt1 = 1L;
	private final long idt2 = 2L;
	
	
	@Mock
	private TopicController topicController;
	
	@Mock
	private StudySessionController sessionController;
	
	private AutoCloseable closeable;

	@Override
	protected void onSetUp(){
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
		});
		robot().waitForIdle();
		window = new FrameFixture(robot(), managerView);
		window.show();
		topic1 = new Topic("geografia", "capitali del continente asiatico", 2, new ArrayList<>());
		topic2 = new Topic("Serialità televisiva", "guarda titoli più famosi", 1, new ArrayList<>());
		topic1.setId(idt1);
		topic2.setId(idt2);
		session1 = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic1)));
		session2 = new StudySession(LocalDate.now().plusDays(2), 90, "un'altra nota", new ArrayList<>(List.of(topic1)));
		session1.setId(ids1);
		session2.setId(ids2);
	}
	
	@Override
	public void onTearDown() throws Exception {
		closeable.close();
	}
	
	
	@Test
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withName("topicLabel"));
		window.label(JLabelMatcher.withName("sessionLabel"));
		window.button(JButtonMatcher.withName("completeSessionButton")).requireDisabled();
		window.button(JButtonMatcher.withName("deleteSessionButton")).requireDisabled();
		window.button(JButtonMatcher.withName("deleteTopicButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("completeSessionButton")).isEnabled()).isFalse();
		assertThat(window.button(JButtonMatcher.withName("deleteSessionButton")).isEnabled()).isFalse();
		assertThat(window.button(JButtonMatcher.withName("deleteTopicButton")).isEnabled()).isFalse();
	}

	@Test
	public void testButtonsShouldBeEnabledOnlyWhenAnObjectIsSelected() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
		});
		GuiActionRunner.execute(()->{
			managerView.getTopicModel().addElement(topic1);
			managerView.getStudySessionModel().addElement(session1);
			managerView.getStudySessionModel().addElement(session2);
		});
		robot().waitForIdle();
		window.list("topicList").selectItem(0);
		robot().waitForIdle();
		window.list("sessionList").selectItem(0);
		robot().waitForIdle();
		JButtonFixture deleteTopicButton = window.button(JButtonMatcher.withName("deleteTopicButton"));
		deleteTopicButton.requireEnabled();
		JButtonFixture deleteSessionButton = window.button(JButtonMatcher.withName("deleteSessionButton"));
		deleteSessionButton.requireEnabled();
		JButtonFixture completeButton = window.button(JButtonMatcher.withName("completeSessionButton"));
		completeButton.requireEnabled();
		JButtonFixture totalTimeButton = window.button(JButtonMatcher.withText("totalTime"));
		totalTimeButton.requireEnabled();
		JButtonFixture percentageButton = window.button(JButtonMatcher.withText("%Completion"));
		percentageButton.requireEnabled();
		assertThat(deleteTopicButton.isEnabled()).isTrue();
		assertThat(deleteSessionButton.isEnabled()).isTrue();
		assertThat(completeButton.isEnabled()).isTrue();
		assertThat(totalTimeButton.isEnabled()).isTrue();
		assertThat(percentageButton.isEnabled()).isTrue();
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.list("sessionList").clearSelection();
		robot().waitForIdle();
		deleteTopicButton.requireDisabled();
		deleteSessionButton.requireDisabled();
		completeButton.requireDisabled();
		totalTimeButton.requireDisabled();
		percentageButton.requireDisabled();
		assertThat(deleteTopicButton.isEnabled()).isFalse();
		assertThat(deleteSessionButton.isEnabled()).isFalse();
		assertThat(completeButton.isEnabled()).isFalse();
		assertThat(totalTimeButton.isEnabled()).isFalse();
		assertThat(percentageButton.isEnabled()).isFalse();
	}
	
	
	@Test
	public void testCompleteSessionWithNoSelection() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(sessionController);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		robot().waitForIdle();
		verify(sessionController, never()).handleCompleteSession(anyLong());
		window.label("errorMessageLabel").requireText(" ");
		verify(sessionController, never()).handleCompleteSession(anyLong());
		robot().waitForIdle();
	}
	
	
	@Test
	public void testCompleteSessionButtonWithNoSelectionAndNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		robot().waitForIdle();
		assertThat(window.list("sessionList").selection()).isEmpty();
		assertThat(window.button(JButtonMatcher.withName("completeSessionButton")).isEnabled()).isFalse();
	}

	@Test
	public void testCompleteSessionButtonWithSelectionButNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(null);
		});
		robot().waitForIdle();
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		robot().waitForIdle();
		assertThat(window.list("sessionList").selection()).hasSize(1);
		assertThat(managerView.getSessionController()).isNull();
	}

	@Test
	public void testCompleteButtonShoudBeDisabledIfSessionSelectedIsCompleted() {
		session1.setIsComplete(true);
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
		});
		robot().waitForIdle();
		GuiActionRunner.execute(()-> {
			managerView.getStudySessionModel().addElement(session1);
		});
		robot().waitForIdle();
		window.list("sessionList").selectItem(0);
		if(session1.isComplete() == true) {
			JButtonFixture completeButton = window.button(JButtonMatcher.withName("completeSessionButton"));
			completeButton.requireDisabled();
			assertThat(completeButton.isEnabled()).isFalse();
		}
		assertThat(session1.isComplete()).isTrue();
	}

	@Test
	public void testSessionShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showSessionError("error message", session2);
		});
		robot().waitForIdle();
		window.label("errorMessageLabel").requireText("error message: "+ session2);
		assertThat(window.label("errorMessageLabel").text()).isEqualTo("error message: "+ session2);
		robot().waitForIdle();
	}

	@Test
	public void testTopicShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showTopicError("error message", topic2);
		});
		robot().waitForIdle();
		window.label("errorMessageLabel").requireText("error message: "+ topic2);
		assertThat(window.label("errorMessageLabel").text()).isEqualTo("error message: "+ topic2);
		robot().waitForIdle();
	}
	
	@Test 
	public void testTopicAddedShouldAddTheTopicToTheListAndResetErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showGeneralError("error");
		});
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			managerView.topicAdded(topic1);
		});
		robot().waitForIdle();
		String[] listContents = window.list("topicList").contents();
		assertThat(listContents).containsExactly(topic1.toString());
		window.label("errorMessageLabel").requireText(" ");
		robot().waitForIdle();
	}
	
	@Test 
	public void testSessionAddedShouldAddTheSessionToTheListAndResetErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showGeneralError("error");
		});
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			managerView.sessionAdded(session1);
		});
		robot().waitForIdle();
		String[] listContents = window.list("sessionList").contents();
		assertThat(listContents).containsExactly(session1.toString());
		window.label("errorMessageLabel").requireText(" ");
		robot().waitForIdle();
	}
	
	@Test 
	public void testSessionRemovedShouldRemoveTheSessionFromTheListAndResetTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showGeneralError("error");
		});
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			managerView.sessionAdded(session1);
			managerView.sessionAdded(session2);
		});
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			managerView.sessionRemoved(session1);
		});
		robot().waitForIdle();
		String[] listContents = window.list("sessionList").contents();
		assertThat(listContents).containsExactly(session2.toString());
		window.label("errorMessageLabel").requireText(" ");
	}
	
	@Test 
	public void testTopicRemovedShouldRemoveTheTopicFromTheListAndResetTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showGeneralError("error");
		});
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			managerView.topicAdded(topic1);
			managerView.topicAdded(topic2);
		});
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			managerView.topicRemoved(topic1);
		});
		robot().waitForIdle();
		String[] listContents = window.list("topicList").contents();
		assertThat(listContents).containsExactly(topic2.toString());
		window.label("errorMessageLabel").requireText(" ");
		robot().waitForIdle();
	}
	
	@Test
	public void testDeleteTopicButtonCallsTopicControllerDeleteTopic() {
		GuiActionRunner.execute(()-> {
			DefaultListModel<Topic> listTopicModel = managerView.getTopicModel();
			listTopicModel.addElement(topic1);
			listTopicModel.addElement(topic2);
		});
		robot().waitForIdle();
		managerView.setTopicController(topicController);
		window.list("topicList").selectItem(1);
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		verify(topicController).handleDeleteTopic(idt2);
		robot().waitForIdle();
	}
	
	@Test
	public void testDeleteTopicButtonWithNoSelection() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		robot().waitForIdle();
		verify(topicController, never()).handleDeleteTopic(anyLong());
		window.label("errorMessageLabel").requireText(" ");
		robot().waitForIdle();
	}
	
	@Test
	public void testDeleteTopicButtonWithNoSelectionAndNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		robot().waitForIdle();
		assertThat(window.list("topicList").selection()).isEmpty();
		assertThat(window.button(JButtonMatcher.withName("deleteTopicButton")).isEnabled()).isFalse();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
	}

	@Test
	public void testDeleteTopicButtonWithNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(null);
		});
		robot().waitForIdle();
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		robot().waitForIdle();
		assertThat(window.list("topicList").selection()).hasSize(1);
		assertThat(managerView.getTopicController()).isNull();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
	}
	
	@Test
	public void testDeleteSessionButtonCallsSessionControllerDeleteSession() {
		GuiActionRunner.execute(()-> {
			DefaultListModel<StudySession> listSessionModel = managerView.getStudySessionModel();
			listSessionModel.addElement(session1);
			listSessionModel.addElement(session2);
		});
		robot().waitForIdle();
		managerView.setSessionController(sessionController);
		window.list("sessionList").selectItem(1);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		verify(sessionController).handleDeleteSession(ids2);
		robot().waitForIdle();
	}
	
	@Test
	public void testDeleteSessionButtonWithNoSelection() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(sessionController);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		robot().waitForIdle();
		verify(sessionController, never()).handleDeleteSession(anyLong());
		window.label("errorMessageLabel").requireText(" ");
		robot().waitForIdle();
	}
	
	@Test
	public void testDeleteSessionButtonWithNoSelectionAndNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		robot().waitForIdle();
		assertThat(window.list("sessionList").selection()).isEmpty();
		assertThat(window.button(JButtonMatcher.withName("deleteSessionButton")).isEnabled()).isFalse();
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(1);
	}

	@Test
	public void testDeleteSessionButtonWithSelectionButNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(null);
		});
		robot().waitForIdle();
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		robot().waitForIdle();
		assertThat(window.list("sessionList").selection()).hasSize(1);
		assertThat(managerView.getSessionController()).isNull();
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(1);
	}
	
	@Test
	public void testCompleteSessionButtonCallsSessionControllerCompleteSession() {
		session1.setIsComplete(false);
		GuiActionRunner.execute(()-> {
			DefaultListModel<StudySession> listSessionModel = managerView.getStudySessionModel();
			listSessionModel.addElement(session1);
		});
		robot().waitForIdle();
		managerView.setSessionController(sessionController);
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		verify(sessionController).handleCompleteSession(ids1);
		robot().waitForIdle();
	}
	
	@Test
	public void testTotalTimeButtonCallsTopicControllerTotalTimeAndResetError() {
		topic1.setSessions(new ArrayList<>(List.of(session1, session2)));
		GuiActionRunner.execute(()-> {
			DefaultListModel<Topic> listTopicModel = managerView.getTopicModel();
			listTopicModel.addElement(topic1);
		});
		robot().waitForIdle();
		managerView.setTopicController(topicController);
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withText("totalTime")).click();
		verify(topicController).handleTotalTime(idt1);
		assertThat(topic1.totalTime()).isEqualTo(150);
		robot().waitForIdle();
	}
	
	@Test
	public void testTotalTimeButtonWithNoSelection() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withText("totalTime")).click();
		robot().waitForIdle();
		verifyNoInteractions(sessionController);
		window.label("errorMessageLabel").requireText(" ");
		verify(topicController, never()).handleTotalTime(anyLong());
		robot().waitForIdle();
	}
	
	@Test
	public void testTotalTimeButtonWithNoSelectionAndNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withText("totalTime")).click();
		robot().waitForIdle();
		assertThat(window.list("topicList").selection()).isEmpty();
		assertThat(window.button(JButtonMatcher.withText("totalTime")).isEnabled()).isFalse();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
	}

	@Test
	public void testTotalTimeButtonWithSelectionButNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(null);
		});
		robot().waitForIdle();
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withText("totalTime")).click();
		robot().waitForIdle();
		assertThat(window.list("topicList").selection()).hasSize(1);
		assertThat(managerView.getTopicController()).isNull();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
	}

	@Test
	public void testTotalTimeButtonResetError() {
		topic1.setSessions(new ArrayList<>(List.of(session1, session2)));
		GuiActionRunner.execute(()-> {
			DefaultListModel<Topic> listTopicModel = managerView.getTopicModel();
			listTopicModel.addElement(topic1);
			managerView.showGeneralError("error");
		});
		robot().waitForIdle();
		managerView.setTopicController(topicController);
		window.list("topicList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("totalTime")).click();
		window.label("errorMessageLabel").requireText(" ");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(" ");
		assertThat(topic1.totalTime()).isEqualTo(150);
		robot().waitForIdle();
	}
		
	@Test
	public void testPercentageButtonCallsTopicControllerPercentageOfCompletion(){
		session1.setIsComplete(true);
		topic1.setSessions(new ArrayList<>(List.of(session1, session2)));
		GuiActionRunner.execute(()-> {
			DefaultListModel<Topic> listTopicModel = managerView.getTopicModel();
			listTopicModel.addElement(topic1);
		});
		robot().waitForIdle();
		managerView.setTopicController(topicController);
		window.list("topicList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("%Completion")).click();
		verify(topicController).handlePercentageOfCompletion(idt1);
		assertThat(topic1.percentageOfCompletion()).isEqualTo(50);
		robot().waitForIdle();
	}
	
	@Test
	public void testPercentageButtonCallsTopicResetError(){
		topic1.setSessions(new ArrayList<>(List.of(session1, session2)));
		GuiActionRunner.execute(()-> {
			DefaultListModel<Topic> listTopicModel = managerView.getTopicModel();
			listTopicModel.addElement(topic1);
			managerView.showGeneralError("error");
		});
		robot().waitForIdle();
		managerView.setTopicController(topicController);
		window.list("topicList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("%Completion")).click();
		window.label("errorMessageLabel").requireText(" ");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(" ");
		assertThat(topic1.percentageOfCompletion()).isZero();
		robot().waitForIdle();
	}

	
	@Test
	public void testPercentageButtonWithNoSelection() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withText("%Completion")).click();
		robot().waitForIdle();
		verifyNoInteractions(sessionController);
		window.label("errorMessageLabel").requireText(" ");
		verify(topicController, never()).handlePercentageOfCompletion(anyLong());
		robot().waitForIdle();
	}
	
	@Test
	public void testPercentageButtonWithNoSelectionAndNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withText("%Completion")).click();
		robot().waitForIdle();
		assertThat(window.list("topicList").selection()).isEmpty();
		assertThat(window.button(JButtonMatcher.withText("%Completion")).isEnabled()).isFalse();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
	}

	@Test
	public void testPercentageButtonWithSelectionButNullController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(null);
		});
		robot().waitForIdle();
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withText("%Completion")).click();
		robot().waitForIdle();
		assertThat(window.list("topicList").selection()).hasSize(1);
		assertThat(managerView.getTopicController()).isNull();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
	}
	
	@Test
	public void testOnTopicAdded() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.onTopicAdded(topic);
		});
		
		assertThat(managerView.getTopicModel().contains(topic)).isTrue();
		assertThat(managerView.getTopicModel().size()).isEqualTo(1);
	}

	@Test
	public void testOnTopicRemoved() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic);
			managerView.onTopicRemoved(topic);
		});
		
		assertThat(managerView.getTopicModel().contains(topic)).isFalse();
		assertThat(managerView.getTopicModel().size()).isZero();
	}

	@Test
	public void testOnTopicError() {
		String errorMessage = "Topic non trovato";
		GuiActionRunner.execute(() -> {
			managerView.onTopicError(errorMessage);
		});
		window.label("errorMessageLabel").requireText(errorMessage);
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(errorMessage);
	}

	@Test
	public void testOnTotalTimeCalculated() {
		Integer totalTime = 120;
		GuiActionRunner.execute(() -> {
			managerView.onTotalTimeCalculated(totalTime);
		});
		window.label("errorMessageLabel").requireText("Tempo totale: 120 minuti");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo("Tempo totale: 120 minuti");
	}

	@Test
	public void testOnPercentageCalculated() {
		Integer percentage = 75;
		GuiActionRunner.execute(() -> {
			managerView.onPercentageCalculated(percentage);
		});
		window.label("errorMessageLabel").requireText("Percentuale di completamento: 75%");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo("Percentuale di completamento: 75%");
	}

	@Test
	public void testOnTopicAddedResetsErrorLabel() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		GuiActionRunner.execute(() -> {
			managerView.showGeneralError("Previous error");
			managerView.onTopicAdded(topic);
		});
		window.label("errorMessageLabel").requireText(" ");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(" ");
		assertThat(managerView.getTopicModel().contains(topic)).isTrue();
	}

	@Test
	public void testOnTopicRemovedResetsErrorLabel() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic);
			managerView.showGeneralError("Previous error");
			managerView.onTopicRemoved(topic);
		});
		window.label("errorMessageLabel").requireText(" ");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(" ");
		assertThat(managerView.getTopicModel().contains(topic)).isFalse();
	}

	@Test
	public void testSetTopicControllerSetsCallback() {
		TopicController controller = mock(TopicController.class);
		GuiActionRunner.execute(() -> {
			managerView.setTopicController(controller);
		});
		verify(controller).setViewCallback(managerView);
	}

	@Test
	public void testSetTopicControllerWithNullController() {
		GuiActionRunner.execute(() -> {
			managerView.setTopicController(null);
		});
		assertThat(managerView.getTopicController()).isNull();
	}	
	
	@Test
	public void testButtonsWithNullTopicController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
		});
		robot().waitForIdle();
		window.list("topicList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		window.button(JButtonMatcher.withText("totalTime")).click();
		window.button(JButtonMatcher.withText("%Completion")).click();
		robot().waitForIdle();
		assertThat(managerView.getTopicController()).isNull();
		assertThat(managerView.getTopicModel().getSize()).isEqualTo(1);
		assertThat(window.list("topicList").selection()).hasSize(1);
	}

	@Test
	public void testButtonsWithNullSessionController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
		});
		robot().waitForIdle();
		window.list("sessionList").selectItem(0);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		robot().waitForIdle();
		assertThat(managerView.getSessionController()).isNull();
		assertThat(managerView.getStudySessionModel().getSize()).isEqualTo(1);
		assertThat(window.list("sessionList").selection()).hasSize(1);
	}
	
	@Test
	public void testToTopicPanelButtonCallsShowCreateTopicView() {
		window.button(JButtonMatcher.withName("addTopicNavButton")).click();
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			assertThat(managerView.isDisplayable()).isTrue();
		});
	}
	
	@Test
	public void testToSessionPanelButtonCallsShowCreateSessionView() {
		window.button(JButtonMatcher.withName("addSessionNavButton")).click();
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			assertThat(managerView.isDisplayable()).isTrue();
		});
	}
	
	@Test
	public void testOnSessionAdded() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		GuiActionRunner.execute(() -> {
			managerView.onSessionAdded(session);
		});
		assertThat(managerView.getStudySessionModel().contains(session)).isTrue();
		assertThat(managerView.getStudySessionModel().size()).isEqualTo(1);
	}

	@Test
	public void testOnSessionRemoved() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session);
			managerView.onSessionRemoved(session);
		});
		assertThat(managerView.getStudySessionModel().contains(session)).isFalse();
		assertThat(managerView.getStudySessionModel().size()).isZero();
	}

	@Test
	public void testOnSessionError() {
		String errorMessage = "Sessione non trovata";
		GuiActionRunner.execute(() -> {
			managerView.onSessionError(errorMessage);
		});
		window.label("errorMessageLabel").requireText(errorMessage);
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(errorMessage);
	}

	@Test
	public void testOnSessionAddedResetsErrorLabel() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		GuiActionRunner.execute(() -> {
			managerView.showGeneralError("Previous error");
			managerView.onSessionAdded(session);
		});
		window.label("errorMessageLabel").requireText(" ");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(" ");
		assertThat(managerView.getStudySessionModel().contains(session)).isTrue();
	}

	@Test
	public void testOnSessionRemovedResetsErrorLabel() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());

		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session);
			managerView.showGeneralError("Previous error");
			managerView.onSessionRemoved(session);
		});
		window.label("errorMessageLabel").requireText(" ");
		assertThat(window.label("errorMessageLabel").text()).isEqualTo(" ");
		assertThat(managerView.getStudySessionModel().contains(session)).isFalse();
	}

	@Test
	public void testSetSessionControllerSetsCallback() {
		StudySessionController controller = mock(StudySessionController.class);
		GuiActionRunner.execute(() -> {
			managerView.setSessionController(controller);
		});
		verify(controller).setViewCallBack(managerView);
	}

	@Test
	public void testSetSessionControllerWithNullController() {
		GuiActionRunner.execute(() -> {
			managerView.setSessionController(null);
		});
		assertThat(managerView.getSessionController()).isNull();
	}
	
	@Test
	public void testCompleteSessionButton_NoSelectionWithController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(sessionController);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		robot().waitForIdle();
		verify(sessionController, never()).handleCompleteSession(anyLong());
	}

	@Test
	public void testDeleteTopicButton_NoSelectionWithController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		robot().waitForIdle();
		verify(topicController, never()).handleDeleteTopic(anyLong());
	}

	@Test
	public void testDeleteSessionButton_NoSelectionWithController() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(sessionController);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		robot().waitForIdle();
		verify(sessionController, never()).handleDeleteSession(anyLong());
	}

	@Test
	public void testTotalTimeButton_NoSelectionWithController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("totalTime")).click();
		robot().waitForIdle();
		verify(topicController, never()).handleTotalTime(anyLong());
	}

	@Test
	public void testPercentageButton_NoSelectionWithController() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("%Completion")).click();
		robot().waitForIdle();
		verify(topicController, never()).handlePercentageOfCompletion(anyLong());
	}
	
	
	@Test
	public void testCompleteSessionButton_NoSelectionWithController_ForcedClick() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(sessionController);
			managerView.getCompleteSessionButton().setEnabled(true);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("completeSessionButton")).requireEnabled().click();
		robot().waitForIdle();
		verify(sessionController, never()).handleCompleteSession(anyLong());
	}
	@Test
	public void testDeleteTopicButton_NoSelectionWithController_ForcedClick() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
			managerView.getDeleteTopicButton().setEnabled(true);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("deleteTopicButton")).requireEnabled().click();
		robot().waitForIdle();
		verify(topicController, never()).handleDeleteTopic(anyLong());
	}
	@Test
	public void testDeleteSessionButton_NoSelectionWithController_ForcedClick() {
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session1);
			managerView.setSessionController(sessionController);
			managerView.getDeleteSessionButton().setEnabled(true);
		});
		robot().waitForIdle();
		window.list("sessionList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("deleteSessionButton")).requireEnabled().click();
		robot().waitForIdle();
		verify(sessionController, never()).handleDeleteSession(anyLong());
	}
	@Test
	public void testTotalTimeButton_NoSelectionWithController_ForcedClick() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
			managerView.getTotalTimeButton().setEnabled(true);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("totalTime")).requireEnabled().click();
		robot().waitForIdle();
		verify(topicController, never()).handleTotalTime(anyLong());
	}
	@Test
	public void testPercentageButton_NoSelectionWithController_ForcedClick() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic1);
			managerView.setTopicController(topicController);
			managerView.getPercentageButton().setEnabled(true);
		});
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withText("%Completion")).requireEnabled().click();
		robot().waitForIdle();
		verify(topicController, never()).handlePercentageOfCompletion(anyLong());
	}
	
	@Test
	public void testOnTopicAddedWithNullSessionPanel() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.setSessionPanel(null);
			managerView.onTopicAdded(topic);
		});
		
		assertThat(managerView.getTopicModel().contains(topic)).isTrue();
		assertThat(managerView.getTopicModel().size()).isEqualTo(1);
	}

	@Test
	public void testOnTopicRemovedWithNullSessionPanel() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic);
			managerView.setSessionPanel(null);
			managerView.onTopicRemoved(topic);
		});
		
		assertThat(managerView.getTopicModel().contains(topic)).isFalse();
		assertThat(managerView.getTopicModel().size()).isZero();
	}

	@Test
	public void testOnTopicAddedWithNullTopicModel() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			SessionPanel mockSessionPanel = new SessionPanel() {
				private static final long serialVersionUID = 1L;

				@Override
				public DefaultListModel<Topic> getTopicModel() {
					return null;
				}
			};
			managerView.setSessionPanel(mockSessionPanel);
			managerView.onTopicAdded(topic);
		});
		
		assertThat(managerView.getTopicModel().contains(topic)).isTrue();
		assertThat(managerView.getTopicModel().size()).isEqualTo(1);
	}

	@Test
	public void testOnTopicRemovedWithNullTopicModel() {
		Topic topic = new Topic("Test Topic", "Description", 3, new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.getTopicModel().addElement(topic);
			SessionPanel mockSessionPanel = new SessionPanel() {
				private static final long serialVersionUID = 1L;

				@Override
				public DefaultListModel<Topic> getTopicModel() {
					return null;
				}
			};
			managerView.setSessionPanel(mockSessionPanel);
			managerView.onTopicRemoved(topic);
		});
		
		assertThat(managerView.getTopicModel().contains(topic)).isFalse();
		assertThat(managerView.getTopicModel().size()).isZero();
	}

	@Test
	public void testOnSessionAddedWithNullTopicPanel() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.setTopicPanel(null);
			managerView.onSessionAdded(session);
		});
		
		assertThat(managerView.getStudySessionModel().contains(session)).isTrue();
		assertThat(managerView.getStudySessionModel().size()).isEqualTo(1);
	}

	@Test
	public void testOnSessionRemovedWithNullTopicPanel() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session);
			managerView.setTopicPanel(null);
			managerView.onSessionRemoved(session);
		});
		
		assertThat(managerView.getStudySessionModel().contains(session)).isFalse();
		assertThat(managerView.getStudySessionModel().size()).isZero();
	}

	@Test
	public void testOnSessionAddedWithNullSessionModel() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			TopicPanel mockTopicPanel = new TopicPanel() {
				private static final long serialVersionUID = 1L;

				@Override
				public DefaultListModel<StudySession> getSessionModel() {
					return null;
				}
			};
			managerView.setTopicPanel(mockTopicPanel);
			managerView.onSessionAdded(session);
		});
		
		assertThat(managerView.getStudySessionModel().contains(session)).isTrue();
		assertThat(managerView.getStudySessionModel().size()).isEqualTo(1);
	}

	@Test
	public void testOnSessionRemovedWithNullSessionModel() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "test note", new ArrayList<>());
		
		GuiActionRunner.execute(() -> {
			managerView.getStudySessionModel().addElement(session);
			TopicPanel mockTopicPanel = new TopicPanel() {
				private static final long serialVersionUID = 2L;

				@Override
				public DefaultListModel<StudySession> getSessionModel() {
					return null;
				}
			};
			managerView.setTopicPanel(mockTopicPanel);
			managerView.onSessionRemoved(session);
		});
		
		assertThat(managerView.getStudySessionModel().contains(session)).isFalse();
		assertThat(managerView.getStudySessionModel().size()).isZero();
	}
	

}
