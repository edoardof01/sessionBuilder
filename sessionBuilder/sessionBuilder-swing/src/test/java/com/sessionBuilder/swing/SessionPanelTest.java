package com.sessionBuilder.swing;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.ComponentMatcher;
import org.assertj.swing.core.GenericTypeMatcher;
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
import com.sessionBuilder.core.StudySessionController;
import com.sessionBuilder.core.Topic;
import com.toedter.calendar.JDateChooser;

@RunWith(GUITestRunner.class)
public class SessionPanelTest extends AssertJSwingJUnitTestCase {
	
	private FrameFixture window;
	private TopicAndSessionManager managerView;

	private LocalDate date;
	private int duration;
	private String note;
	private StudySession session;
	private Topic topic;
	@Mock
	private StudySessionController sessionController;
	
	private AutoCloseable closeable;
	
	@Override
	protected void onSetUp() throws Exception {
		
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			managerView = new TopicAndSessionManager();
		});
		window = new FrameFixture(robot(), managerView);
		window.show();
		date = LocalDate.now().plusDays(1);
		duration = 60;
		note = "una nota sulla sessione";
		topic = new Topic("cartoleria", "acquista materiale", 2, new ArrayList<>());
		session = new StudySession(date, duration, note, new ArrayList<>(List.of(topic)));
		managerView.getSessionPanel().setManagerView(managerView);
		
		GuiActionRunner.execute(() -> {
			managerView.showCreateSessionView();
		});
	}
	
	@Override
	public void onTearDown() throws Exception {
		window.cleanUp();
		closeable.close();
	}
	
	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Date:"));
		window.label(JLabelMatcher.withText("Duration:"));
		window.label(JLabelMatcher.withText("Note:"));
		window.label(JLabelMatcher.withText("Topics:"));
		
		ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
			@Override
			protected boolean isMatching(JDateChooser component) {
				return "dateChooser".equals(component.getName());
			}
		};
		Component dateChooser = robot().finder().find(matcher);
		assertThat(dateChooser).isNotNull();
		
		window.textBox("durationField").requireEnabled();
		window.textBox("noteField").requireEnabled();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
	}
	
	@Test @GUITest
	public void testWhenFieldsAreNotEmptyAddButtonIsEnabled() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		robot().waitForIdle();
		window.textBox("durationField").enterText(String.valueOf(duration));
		window.textBox("noteField").enterText(note);
		window.list("sessionPanelTopicList").selectItem(0);
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(javaDate);
		});
		robot().waitForIdle();
		
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
	}

	@Test @GUITest
	public void testTopicSelectionEnablesButton() {
		setDateChooserValue(date);
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("test");
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		window.list("sessionPanelTopicList").selectItem(0);
		robot().waitForIdle();
		
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
	}

	@Test @GUITest
	public void testDateChangeEnablesButton() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("test");
		window.list("sessionPanelTopicList").selectItem(0);
		
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		setDateChooserValue(date);
		
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
	}

	@Test @GUITest
	public void testWhenEitherDurationFieldorNoteFieldAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture durationText = window.textBox("durationField");
		JTextComponentFixture noteText = window.textBox("noteField");
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});

		setDateChooserValue(date);
		durationText.enterText(String.valueOf(duration));
		noteText.enterText("");
		window.list("sessionPanelTopicList").selectItem(0);
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();

		durationText.deleteText();
		noteText.deleteText();
		durationText.enterText("");
		noteText.enterText(note);
		window.list("sessionPanelTopicList").selectItem(0);
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		
		window.list("sessionPanelTopicList").clearSelection();
		durationText.enterText("60");
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
	}

	@Test @GUITest
	public void testWhenDateIsBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture durationText = window.textBox("durationField");
		JTextComponentFixture noteText = window.textBox("noteField");
		
		durationText.enterText(String.valueOf(duration));
		noteText.enterText(note);
		clearDateChooser();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
	}

	@Test @GUITest
	public void testSessionShowTheMessageInTheErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.showCreateSessionView();
			managerView.getSessionPanel().showSessionError("error message", session);
		});
		window.label("sessionErrorMessage").requireText("error message: "+ session);
		assertThat(window.label("sessionErrorMessage").text()).isEqualTo("error message: "+ session);
	}

	@Test @GUITest
	public void testAddButtonDisabledWithoutSelectingATopic() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		robot().waitForIdle();
		window.textBox("durationField").enterText(String.valueOf(duration));
		window.textBox("noteField").enterText(note);
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			Date javaDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(javaDate);
		});
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
	}

	@Test @GUITest
	public void testKeyTypingEnablesDisablesButton() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		window.textBox("durationField").focus().pressAndReleaseKeys(KeyEvent.VK_6, KeyEvent.VK_0);
		window.textBox("noteField").focus().pressAndReleaseKeys(KeyEvent.VK_A);
		window.list("sessionPanelTopicList").selectItem(0);
		setDateChooserValue(date);
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
	}

	@Test @GUITest
	public void testKeyAdapterWithTopicSelection() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		setDateChooserValue(date);
		window.list("sessionPanelTopicList").selectItem(0);
		
		window.textBox("durationField").focus().enterText("6");
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		
		window.textBox("durationField").focus().enterText("0");
		window.textBox("noteField").focus().enterText("a");
		robot().waitForIdle();
		
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();
	}

	@Test @GUITest
	public void testAddButtonEnabledifClickedShouldResetErrorLabel() {
		GuiActionRunner.execute(() -> {
			managerView.getTopicPanel().showGeneralError("error");
		});
		setDateChooserValue(LocalDate.now().plusDays(1));
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("una nota");
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		robot().waitForIdle();
		window.list("sessionPanelTopicList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		window.label("sessionErrorMessage").requireText("");
		assertThat(window.label("sessionErrorMessage").text()).isEmpty();
	}
	
	@Test @GUITest
	public void testAddButtonCallsSessionControllerCreateSession() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
			managerView.getSessionPanel().setSessionController(sessionController);
		});
		robot().waitForIdle();
		JTextComponentFixture noteField = window.textBox("noteField").enterText("test");
		JTextComponentFixture durationField = window.textBox("durationField").enterText("60");
		LocalDate newDate = LocalDate.now().plusMonths(1);
		setDateChooserValue(newDate);
		robot().waitForIdle();
		window.list("sessionPanelTopicList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		verify(sessionController).handleCreateSession(newDate, Integer.parseInt(durationField.text()), noteField.text(), new ArrayList<>(List.of(topic)));
	}
	
	@Test @GUITest
	public void testAddButtonHandlesException() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
			managerView.getSessionPanel().setSessionController(sessionController);
			when(sessionController.handleCreateSession(any(), anyInt(), anyString(), any()))
				.thenThrow(new RuntimeException("Test exception"));
		});
		setDateChooserValue(date);
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("test");
		window.list("sessionPanelTopicList").selectItem(0);

		window.button(JButtonMatcher.withName("addSessionButton")).click();

		window.label("sessionErrorMessage").requireText("Errore nel salvare la sessione: Test exception");
		assertThat(window.label("sessionErrorMessage").text()).isEqualTo("Errore nel salvare la sessione: Test exception");
	}

	@Test @GUITest
	public void testListSelectionWithAllFieldsFilled() {
		setDateChooserValue(date);
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("test");

		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});

		window.list("sessionPanelTopicList").selectItem(0);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).requireEnabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isTrue();

		window.list("sessionPanelTopicList").clearSelection();
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
	}
	
	@Test @GUITest
	public void testAddButtonEnabledWhenDateIsSelectedButNoteNotFailure() {
		JTextComponentFixture durationText = window.textBox("durationField");
		JTextComponentFixture noteText = window.textBox("noteField");
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().getTopicModel().addElement(topic);
		});
		durationText.enterText(String.valueOf(duration));
		noteText.enterText("");
		window.list("sessionPanelTopicList").selectItem(0);
		setDateChooserValue(date);
		robot().waitForIdle();
		window.button(JButtonMatcher.withName("addSessionButton")).requireDisabled();
		assertThat(window.button(JButtonMatcher.withName("addSessionButton")).isEnabled()).isFalse();
		assertThat(noteText.text()).isEmpty();
	}

	@Test @GUITest
	public void testBackSessionButtonWithNullManagerViewDoesNothing() {
		GuiActionRunner.execute(() -> {
			managerView.getSessionPanel().setManagerView(null);
		});
		window.button(JButtonMatcher.withName("backSessionButton")).click();
		robot().waitForIdle();
		assertThat(window.button(JButtonMatcher.withName("backSessionButton")).isEnabled()).isTrue();
	}
	
	@Test @GUITest
	public void testBackSessionButtonWithManagerViewCallsShowMainView() {
		window.button(JButtonMatcher.withName("backSessionButton")).click();
		robot().waitForIdle();
		GuiActionRunner.execute(() -> {
			assertThat(managerView.isDisplayable()).isTrue();
		});
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

	private void clearDateChooser() {
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			dateChooser.setDate(null);
		});
		robot().waitForIdle();
	}
	
	

}