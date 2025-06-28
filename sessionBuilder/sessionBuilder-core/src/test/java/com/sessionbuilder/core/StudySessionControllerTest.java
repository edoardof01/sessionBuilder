package com.sessionbuilder.core;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.mockito.InjectMocks;
import org.mockito.Mock;

@RunWith(MockitoJUnitRunner.class)
public class StudySessionControllerTest {

	@Mock
	private StudySessionInterface service;
	
	@Mock
	private SessionViewCallback viewCallback;
	
	@InjectMocks
	private StudySessionController sessionController;
	
	private StudySession session;
	private Topic topic;
	private Topic topic1;
	
	private LocalDate date;
	private int duration;
	private String note;
	private ArrayList<Topic> topics;
	
	private final long ids1 = 1L;
	private final long idt1 = 2L;
	private final long idt2 = 3L;
	
	@Before
	public void setup() {
		date = LocalDate.now().plusDays(1);
		duration = 60;
		note = "una nota";
		topic1 = new Topic();
		topic1.setId(idt2);
		topic = new Topic();
		topic.setId(idt1);
		topics = new ArrayList<>(List.of(topic));
		session = new StudySession(date, duration, note, new ArrayList<>(List.of(topic)));
	}
	
	@Test
	public void testSetViewCallback() {
		SessionViewCallback newCallback = org.mockito.Mockito.mock(SessionViewCallback.class);
		sessionController.setViewCallBack(newCallback);
		assertThat(sessionController.getViewCallback()).isEqualTo(newCallback);
	}
	
	@Test
	public void testHandleCreateStudySessionSuccess() {
		when(service.createSession(date, duration, note, topics)).thenReturn(session);
		StudySession result = sessionController.handleCreateSession(date, duration, note, topics);
		verify(service).createSession(date, duration, note, topics);
		verify(viewCallback).onSessionAdded(session);
		assertThat(result).isEqualTo(session);
	}
	
	@Test
	public void testHandleCreateStudySessionWithException() {
		RuntimeException exception = new RuntimeException("creation failed");
		when(service.createSession(date, duration, note, topics)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, ()->{
			sessionController.handleCreateSession(date, duration, note, topics);
		});
		verify(service).createSession(date, duration, note, topics);
		verify(viewCallback).onSessionError("Error: creation failed");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test 
	public void testHandleCreateSessionWithNullCallBack() {
		sessionController.setViewCallBack(null);
		when(service.createSession(date, duration, note, topics)).thenReturn(session);
		StudySession result = sessionController.handleCreateSession(date, duration, note, topics);
		verify(service).createSession(date, duration, note, topics);
		assertThat(result).isEqualTo(session);
	}
	
	@Test
	public void testHandleCreateSessionWithNullCallbackAndException() {
	   sessionController.setViewCallBack(null);
	   RuntimeException exception = new RuntimeException("creation failed");
	   when(service.createSession(date, duration, note, topics)).thenThrow(exception);
	   RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
	   	sessionController.handleCreateSession(date, duration, note, topics);
	   });
	   verify(service).createSession(date, duration, note, topics);
	   assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetSessionSuccess() {
		when(service.getSessionById(ids1)).thenReturn(session);
		StudySession result = sessionController.handleGetSession(ids1);
		verify(service).getSessionById(ids1);
		assertThat(result).isEqualTo(session);
	}
	
	@Test
	public void testHandleGetSessionWithException() {
		RuntimeException exception = new RuntimeException("session not found");
		when(service.getSessionById(ids1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			sessionController.handleGetSession(ids1);
		});
		verify(service).getSessionById(ids1);
		verify(viewCallback).onSessionError("Error: session not found");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetSessionWithNullCallback() {
		sessionController.setViewCallBack(null);
		RuntimeException exception = new RuntimeException("Session not found");
		when(service.getSessionById(ids1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			sessionController.handleGetSession(ids1);
		});
		verify(service).getSessionById(ids1);
		assertThat(thrown).isEqualTo(exception);
	}

	@Test
	public void testHandleGetAllSessionsSuccess() {
		StudySession sessionTest = new StudySession();
		List<StudySession> allSessions = new ArrayList<>(List.of(session, sessionTest));
		when(service.getAllSessions()).thenReturn(allSessions);
		List<StudySession> result = sessionController.handleGetAllSessions();
		assertThat(result).isEqualTo(allSessions);
		verify(service).getAllSessions();
	}
	
	@Test
	public void testHandleGetAllSessionsWithExceptions() {
	   RuntimeException exception = new RuntimeException("lista di sessioni non estratta");
	   when(service.getAllSessions()).thenThrow(exception);
	   RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
	   	sessionController.handleGetAllSessions();
	   });
	   verify(service).getAllSessions();
	   verify(viewCallback).onSessionError("Errore nel caricamento delle session");
	   assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetAllTopicsWithExceptions() {
		RuntimeException exception = new RuntimeException("lista di sessioni non estratta");
		when(service.getAllSessions()).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			sessionController.handleGetAllSessions();
		});
		verify(service).getAllSessions();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetAllTopicsWithNullCallback() {
		sessionController.setViewCallBack(null);
		RuntimeException exception = new RuntimeException("lista di sessioni non estratta");
		when(service.getAllSessions()).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			sessionController.handleGetAllSessions();
		});
		verify(service).getAllSessions();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleAddTopicSuccess() {
		sessionController.handleAddTopic(ids1, idt2);
		verify(service).addTopic(ids1, idt2);
		verify(viewCallback, never()).onSessionError(org.mockito.ArgumentMatchers.anyString());
	}
	
	@Test
	public void testHandleAddTopicWithException() {
		RuntimeException exception = new RuntimeException("Add topic failed");
		doThrow(exception).when(service).addTopic(ids1, idt2);
		sessionController.handleAddTopic(ids1, idt2);
		verify(service).addTopic(ids1, idt2);
		verify(viewCallback).onSessionError("Error: Add topic failed");
	}
	
	@Test
	public void testHandleAddTopicWithNullCallback() {
		sessionController.setViewCallBack(null);
		RuntimeException exception = new RuntimeException("Add topic failed");
		doThrow(exception).when(service).addTopic(ids1, idt2);
		sessionController.handleAddTopic(ids1, idt2);
		verify(service).addTopic(ids1, idt2);
	}
	
	@Test
	public void testHandleRemoveTopicSuccess() {
		session.setTopics(new ArrayList<>(List.of(topic,topic1)));
		sessionController.handleRemoveTopic(ids1,idt2);
		verify(service).removeTopic(ids1, idt2);
		verify(viewCallback, never()).onSessionError(org.mockito.ArgumentMatchers.anyString());
	}
	
	@Test
	public void testHandleRemoveTopicWithException() {
		RuntimeException exception = new RuntimeException("Remove topic failed");
		doThrow(exception).when(service).removeTopic(ids1, idt2);
		sessionController.handleRemoveTopic(ids1, idt2);
		verify(service).removeTopic(ids1, idt2);
		verify(viewCallback).onSessionError("Error: Remove topic failed");
	}
	
	@Test
	public void testHandleRemoveTopicWithNullCallback() {
		sessionController.setViewCallBack(null);
		RuntimeException exception = new RuntimeException("Remove topic failed");
		doThrow(exception).when(service).removeTopic(ids1, idt2);
		sessionController.handleRemoveTopic(ids1, idt2);
		verify(service).removeTopic(ids1, idt2);
	}
	
	@Test
	public void testHandleCompleteSession() {
		when(service.completeSession(ids1)).thenReturn(session);
		sessionController.handleCompleteSession(ids1);
		verify(service).completeSession(ids1);
		verify(viewCallback).onSessionUpdated(session);
		verify(viewCallback, never()).onSessionError(org.mockito.ArgumentMatchers.anyString());
	}
	
	@Test
	public void testHandleCompleteSessionWithException() {
		RuntimeException exception = new RuntimeException("Complete session failed");
		when(service.completeSession(ids1)).thenThrow(exception);
		sessionController.handleCompleteSession(ids1);
		verify(service).completeSession(ids1);
		verify(viewCallback).onSessionError("Error: Complete session failed");
	}
	
	@Test
	public void testHandleCompleteSessionWithNullCallback() {
		sessionController.setViewCallBack(null);
		RuntimeException exception = new RuntimeException("Complete session failed");
		doThrow(exception).when(service).completeSession(ids1);
		sessionController.handleCompleteSession(ids1);
		verify(service).completeSession(ids1);
	}
	
	@Test
	public void testHandleCompleteSessionWithNullCallbackSuccess() {
		sessionController.setViewCallBack(null);
		when(service.completeSession(ids1)).thenReturn(session);
		sessionController.handleCompleteSession(ids1);
		verify(service).completeSession(ids1);
	}
	
	@Test
	public void testHandleDeleteSession() {
		when(service.getSessionById(ids1)).thenReturn(session);
		session.setId(ids1);
		sessionController.handleDeleteSession(ids1);
		verify(service).deleteSession(ids1);
		verify(viewCallback).onSessionRemoved(session);
	}
	
	@Test
	public void testHandleDeleteSessionWithException() {
		RuntimeException exception = new RuntimeException("Delete session failed");
		when(service.getSessionById(ids1)).thenThrow(exception);
		sessionController.handleDeleteSession(ids1);
		verify(service).getSessionById(ids1);
		verify(service, never()).deleteSession(ids1);
		verify(viewCallback).onSessionError("Error: Delete session failed");
		verify(viewCallback, never()).onSessionRemoved(org.mockito.ArgumentMatchers.any());
	}
	
	@Test
	public void testHandleDeleteSessionWithNullCallback() {
		sessionController.setViewCallBack(null);
		when(service.getSessionById(ids1)).thenReturn(session);
		session.setId(ids1);
		sessionController.handleDeleteSession(ids1);
		verify(service).getSessionById(ids1);
		verify(service).deleteSession(ids1);
	}
	
	@Test
	public void testHandleDeleteSessionWithNullCallbackAndException() {
	   sessionController.setViewCallBack(null);
	   RuntimeException exception = new RuntimeException("Delete session failed");
	   when(service.getSessionById(ids1)).thenThrow(exception);
	   sessionController.handleDeleteSession(ids1);
	   verify(service).getSessionById(ids1);
	   verify(service, never()).deleteSession(ids1);
	}

}
