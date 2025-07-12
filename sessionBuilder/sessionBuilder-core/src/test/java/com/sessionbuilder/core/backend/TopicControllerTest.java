package com.sessionbuilder.core.backend;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TopicControllerTest {
	
	@Mock
	private TopicServiceInterface service;
	
	@Mock
	private TopicViewCallback viewCallback;
	
	@InjectMocks
	private TopicController topicController;
	
	private Topic topic;
	private StudySession session;
	
	private final long idt1 = 1L;
	private final long ids1 = 2L;
	
	private String name;
	private String description;
	private int difficulty;
	
	private AutoCloseable closeable;
	
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		name = "Arredamento";
		description = "abbinamento materiali";
		difficulty = 4;
		topic = new Topic(name, description, difficulty, new ArrayList<>());
		session = new StudySession(LocalDate.now().plusDays(1), 90, "una nota", new ArrayList<>(List.of(topic)));
		session.setId(ids1);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testSetViewCallback() {
		TopicViewCallback newCallback = org.mockito.Mockito.mock(TopicViewCallback.class);
		topicController.setViewCallback(newCallback);
		assertThat(topicController.getViewCallback()).isEqualTo(newCallback);
	}

	
	@Test
	public void testHandleGetTopicSuccess() {
		when(service.getTopicById(idt1)).thenReturn(topic);
		Topic result = topicController.handleGetTopicById(idt1);
		verify(service).getTopicById(idt1);
		assertThat(result).isEqualTo(topic);
	}
	
	@Test
	public void testHandleGetTopicWithException() {
		RuntimeException exception = new RuntimeException("il topic non esiste");
		when(service.getTopicById(idt1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handleGetTopicById(idt1);
		});
		verify(service).getTopicById(idt1);
		verify(viewCallback).onTopicError("Topic non trovato: il topic non esiste");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetTopicWithNullCallback() {
		topicController.setViewCallback(null);
		RuntimeException exception = new RuntimeException("Topic not found");
		when(service.getTopicById(idt1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handleGetTopicById(idt1);
		});
		verify(service).getTopicById(idt1);
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetAllTopicsSuccess() {
		Topic topicTest = new Topic();
		List<Topic> allTopics = new ArrayList<>(List.of(topic,topicTest));
		when(service.getAllTopics()).thenReturn(allTopics);
		List<Topic> result = topicController.handleGetAllTopics();
		assertThat(result).isEqualTo(allTopics);
		verify(service).getAllTopics();
	}
	
	@Test
	public void testHandleGetAllTopicsWithExceptions() {
		RuntimeException exception = new RuntimeException("lista di topic non estratta");
		when(service.getAllTopics()).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handleGetAllTopics();
		});
		verify(service).getAllTopics();
		verify(viewCallback).onTopicError("Errore nel caricamento dei topic");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleGetAllTopicsWithNullCallback() {
		topicController.setViewCallback(null);
		RuntimeException exception = new RuntimeException("lista di topic non estratta");
		when(service.getAllTopics()).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handleGetAllTopics();
		});
		verify(service).getAllTopics();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleCreateTopicSuccess() {
		when(service.createTopic(name, description, difficulty, new ArrayList<>())).thenReturn(topic);
		Topic result = topicController.handleCreateTopic(name, description, difficulty, new ArrayList<>());
		assertThat(result).isEqualTo(topic);
		verify(viewCallback).onTopicAdded(topic);
		verify(service).createTopic(name, description, difficulty, new ArrayList<>());
	}
	
	@Test
	public void testHandleCreateTopicWithException() {
		RuntimeException exception = new RuntimeException("Creation failed");
		List<StudySession> sessionList = new ArrayList<>();
		when(service.createTopic(name, description, difficulty, new ArrayList<>())).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handleCreateTopic(name, description, difficulty, sessionList);
		});
		verify(service).createTopic(name, description, difficulty, new ArrayList<>());
		verify(viewCallback).onTopicError("Topic non trovato: Creation failed");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleCreateTopicWithNullCallback() {
		topicController.setViewCallback(null);
		when(service.createTopic(name, description, difficulty, new ArrayList<>())).thenReturn(topic);
		
		Topic result = topicController.handleCreateTopic(name, description, difficulty, new ArrayList<>());
		
		verify(service).createTopic(name, description, difficulty, new ArrayList<>());
		assertThat(result).isEqualTo(topic);
	}
	
	@Test
	public void testHandleCreateTopicWithNullCallbackAndException() {
	topicController.setViewCallback(null);
	RuntimeException exception = new RuntimeException("Creation failed");
	when(service.createTopic(name, description, difficulty, new ArrayList<>())).thenThrow(exception);
	List<StudySession> sessionList = new ArrayList<>();
	RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
	topicController.handleCreateTopic(name, description, difficulty, sessionList);
	});
	assertThat(thrown).isEqualTo(exception);
	}
	
	@Test 
	public void testHandleAddSessionSuccess() {
		topicController.handleAddSessionToTopic(idt1, ids1);
		verify(service).addSessionToTopic(idt1, ids1);
	}
	
	@Test
	public void testHandleAddSessionWithException() {
		RuntimeException exception = new RuntimeException("Add session failed");
		doThrow(exception).when(service).addSessionToTopic(idt1, ids1);
		topicController.handleAddSessionToTopic(idt1, ids1);
		verify(service).addSessionToTopic(idt1, ids1);
		verify(viewCallback).onTopicError("Topic non trovato: Add session failed");
	}
	
	@Test
	public void testHandleAddSessionWithNullCallback() {
		topicController.setViewCallback(null);
		RuntimeException exception = new RuntimeException("Add session failed");
		doThrow(exception).when(service).addSessionToTopic(idt1, ids1);
		topicController.handleAddSessionToTopic(idt1, ids1);
		verify(service).addSessionToTopic(idt1, ids1);
	}

	@Test
	public void testHandleRemoveSessionFromTopic() {
		topicController.handleRemoveSessionFromTopic(idt1, ids1);
		verify(service).removeSessionFromTopic(idt1, ids1);
		verify(viewCallback, never()).onTopicError(org.mockito.ArgumentMatchers.anyString());
	}
	
	@Test
	public void testHandleRemoveSessionWithException() {
		RuntimeException exception = new RuntimeException("Remove session failed");
		doThrow(exception).when(service).removeSessionFromTopic(idt1, ids1);
		topicController.handleRemoveSessionFromTopic(idt1, ids1);
		verify(service).removeSessionFromTopic(idt1, ids1);
		verify(viewCallback).onTopicError("Errore nella rimozione della sessione: Remove session failed");
	}

	@Test
	public void testHandleRemoveSessionWithNullCallback() {
		topicController.setViewCallback(null);
		RuntimeException exception = new RuntimeException("Remove session failed");
		doThrow(exception).when(service).removeSessionFromTopic(idt1, ids1);
		topicController.handleRemoveSessionFromTopic(idt1, ids1);
		verify(service).removeSessionFromTopic(idt1, ids1);
	}
	
	@Test
	public void testHandleTotalTime() {
		when(service.calculateTotalTime(idt1)).thenReturn(90);
		Integer time = topicController.handleTotalTime(idt1);
		verify(service).calculateTotalTime(idt1);
		assertThat(time).isEqualTo(90);
	}
	
	@Test
	public void testHandleTotalTimeWithCallback() {
		Integer totalTime = 120;
		when(service.calculateTotalTime(idt1)).thenReturn(totalTime);
		topicController.handleTotalTime(idt1);
		verify(service).calculateTotalTime(idt1);
		verify(viewCallback).onTotalTimeCalculated(totalTime);
	}
	
	@Test
	public void testHandleTotalTimeWithNullCallback() {
		topicController.setViewCallback(null);
		Integer totalTime = 120;
		when(service.calculateTotalTime(idt1)).thenReturn(totalTime);
		topicController.handleTotalTime(idt1);
		verify(service).calculateTotalTime(idt1);
	}

	@Test
	public void testHandleTotalTimeWithException() {
		RuntimeException exception = new RuntimeException("Total time calculation failed");
		when(service.calculateTotalTime(idt1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handleTotalTime(idt1);
		});
		verify(service).calculateTotalTime(idt1);
		verify(viewCallback).onTopicError("Topic non trovato: Total time calculation failed");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandleTotalTimeWithNullCallbackAndException() {
		topicController.setViewCallback(null);
		RuntimeException exception = new RuntimeException("Total time calculation failed");
		when(service.calculateTotalTime(idt1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
		topicController.handleTotalTime(idt1);
		});
		verify(service).calculateTotalTime(idt1);
		assertThat(thrown).isEqualTo(exception);
	}

	@Test
	public void testPercentageOfCompletion() {
		StudySession completedSession = new StudySession(LocalDate.now(), 60, "nota per sessione completata", new ArrayList<>(List.of(topic)));
		completedSession.setId(3L);
		completedSession.setIsComplete(true);
		when(service.calculatePercentageOfCompletion(idt1)).thenReturn(50);
		Integer result = topicController.handlePercentageOfCompletion(idt1);
		verify(viewCallback).onPercentageCalculated(50);
		assertThat(result).isEqualTo(50);
		verify(service).calculatePercentageOfCompletion(idt1);
	}
	
	@Test
	public void testHandlePercentageWithCallback() {
		Integer percentage = 75;
		when(service.calculatePercentageOfCompletion(idt1)).thenReturn(percentage);
		topicController.handlePercentageOfCompletion(idt1);
		verify(service).calculatePercentageOfCompletion(idt1);
		verify(viewCallback).onPercentageCalculated(percentage);
	}
	
	@Test
	public void testHandlePercentageWithException() {
		RuntimeException exception = new RuntimeException("Percentage calculation failed");
		when(service.calculatePercentageOfCompletion(idt1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			topicController.handlePercentageOfCompletion(idt1);
		});
		verify(service).calculatePercentageOfCompletion(idt1);
		verify(viewCallback).onTopicError("Topic non trovato: Percentage calculation failed");
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testHandlePercentageWithNullCallbackAndException() {
		topicController.setViewCallback(null);
		RuntimeException exception = new RuntimeException("Percentage calculation failed");
		when(service.calculatePercentageOfCompletion(idt1)).thenThrow(exception);
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
		topicController.handlePercentageOfCompletion(idt1);
		});
		verify(service).calculatePercentageOfCompletion(idt1);
		assertThat(thrown).isEqualTo(exception);
	}

	@Test
	public void testHandlePercentageWithNullCallback() {
		topicController.setViewCallback(null);
		Integer percentage = 75;
		when(service.calculatePercentageOfCompletion(idt1)).thenReturn(percentage);
		topicController.handlePercentageOfCompletion(idt1);
		verify(service).calculatePercentageOfCompletion(idt1);
	}
	
	@Test
	public void testDeleteTopic() {
		when(service.getTopicById(idt1)).thenReturn(topic);
		topicController.handleDeleteTopic(idt1);
		verify(service).getTopicById(idt1);
		verify(service).deleteTopic(idt1);
		verify(viewCallback).onTopicRemoved(topic);
		verify(viewCallback, never()).onTopicError(anyString());
	}
	
	@Test
	public void testHandleDeleteTopicWithException() {
		RuntimeException exception = new RuntimeException("Delete failed");
		when(service.getTopicById(idt1)).thenThrow(exception);
		topicController.handleDeleteTopic(idt1);
		verify(service).getTopicById(idt1);
		verify(service, never()).deleteTopic(idt1);
		verify(viewCallback).onTopicError("Topic non trovato: Delete failed");
	}
	
	@Test
	public void testHandleDeleteTopicWithNullCallback() {
		topicController.setViewCallback(null);
		when(service.getTopicById(idt1)).thenReturn(topic);
		topicController.handleDeleteTopic(idt1);
		verify(service).getTopicById(idt1);
		verify(service).deleteTopic(idt1);
	}
	
	@Test
	public void testHandleDeleteTopicWithNullCallbackAndException() {
	topicController.setViewCallback(null);
	RuntimeException exception = new RuntimeException("Delete failed");
	when(service.getTopicById(idt1)).thenThrow(exception);
	topicController.handleDeleteTopic(idt1);
	verify(service).getTopicById(idt1);
	verify(service, never()).deleteTopic(idt1);
	}
	
	

	

	
	

}
