package com.sessionBuilder.swing;



import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import static org.assertj.core.api.Assertions.*;
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
		robot().waitForIdle();
		window.list("topicList").clearSelection();
		window.list("sessionList").clearSelection();
		robot().waitForIdle();
		deleteTopicButton.requireDisabled();
		deleteSessionButton.requireDisabled();
		completeButton.requireDisabled();
		totalTimeButton.requireDisabled();
		percentageButton.requireDisabled();
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
		}
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
	}
	
	@Test 
	public void testSessionShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showMainView();
			managerView.showSessionError("error message", session2);
		});
		robot().waitForIdle();
		window.label("errorMessageLabel").requireText("error message: "+ session2);
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
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	

}
