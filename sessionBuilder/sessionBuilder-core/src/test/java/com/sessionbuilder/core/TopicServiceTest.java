package com.sessionbuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopicServiceTest {
	
	@Mock
	private TopicRepositoryInterface topicRepository;
	
	@Mock
	private StudySessionRepositoryInterface sessionRepository;
	
	@Mock
	private TransactionManager tm;
	
	@InjectMocks
	private TopicService service;
	
	private String name;
	private String description;
	private int difficulty;
	
	private final long idt1 = 1L;
	private final long idt2 = 2L;
	private Topic topic;
	private Topic topic2;
	private StudySession session;
	private StudySession session2;
	private final long ids1 = 1L;
	private final long ids2 = 2L;
	
	
	@Before
	public void setup() {
		when(tm.doInTopicTransaction(any())).thenAnswer(answer -> {
			TopicTransactionCode<?> code = answer.getArgument(0);
			return code.apply(topicRepository);
		});
		when(tm.doInMultiRepositoryTransaction(any())).thenAnswer(answer -> {
			MultiRepositoryTransactionCode<?> code = answer.getArgument(0);
			RepositoryContext context = new RepositoryContext() {
				@Override
				public TopicRepositoryInterface getTopicRepository() {
					return topicRepository;
				}

				@Override
				public StudySessionRepositoryInterface getSessionRepository() {
					return sessionRepository;
				}
			};
			return code.apply(context);
		});
		name = "Cucina";
		description = "Pasticceria";
		difficulty = 3;
		topic = new Topic(name, description, difficulty, new ArrayList<>());
		topic.setId(idt1);
		topic2 = new Topic("Enologia", "Studia la cantina del Chianti", 3, new ArrayList<>());
		topic2.setId(idt2);
		session = new StudySession(LocalDate.now().plusDays(2), 90, "una nota", new ArrayList<>(List.of(topic)));
		session.setId(ids1);
		session2 = new StudySession(LocalDate.now().plusDays(3), 100, "una nuova nota", new ArrayList<>(List.of(topic2)));
		session2.setId(ids2);
	}
	
	@Test
	public void testGetTopicByIdSuccess(){
		when(topicRepository.findById(idt1)).thenReturn(topic);
		Topic result = service.getTopicById(idt1);
		assertThat(result).isEqualTo(topic);
	}
	
	@Test
	public void testGetTopicByIdFailure() {
		when(topicRepository.findById(idt1)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.getTopicById(idt1));
		assertThat(e.getMessage()).isEqualTo("il topic cercato non esiste");
	}
	
	@Test
	public void testGetAllTopicsSuccess() {
		List<Topic> allTopics = new ArrayList<>(List.of(topic,topic2));
		when(topicRepository.findAll()).thenReturn(allTopics);
		List<Topic> result = service.getAllTopics();
		assertThat(result).isEqualTo(allTopics);
	}
	
	@Test
	public void testGetAllTopicsFailure() {
		when(topicRepository.findAll()).thenThrow(new IllegalArgumentException());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.getAllTopics());
		assertThat(e.getMessage()).isEqualTo("Errore durante il caricamento dei topic");
	}
	
	@Test
	public void testCreateTopicSuccess() {
		Topic topic1 = service.createTopic(name, description, difficulty, new ArrayList<>());
		verify(topicRepository,times(1)).save(topic1);
		assertThat(topic1).isNotNull();
		assertThat(topic1.getName()).isEqualTo(name);
		assertThat(topic1.getDescription()).isEqualTo(description);
		assertThat(topic1.getDifficulty()).isEqualTo(difficulty);
	}
	
	@Test
	public void testCreateTopicWithDuplicatedValuesFailure() {
		when(topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty)).thenReturn(topic);
		List<StudySession> list = new ArrayList<>();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()-> service.createTopic(name, description, difficulty, list));
		assertThat(e.getMessage()).isEqualTo("Esiste già un topic con questi valori");
		verify(topicRepository, times(0)).save(any(Topic.class));
	}
	
	@Test
	public void testDeleteTopicSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		service.deleteTopic(idt1);
		verify(topicRepository).delete(idt1);
	}
	
	@Test
	public void testDeleteTopicFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.deleteTopic(10L));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
	}
	
	@Test
	public void testAddSessionSuccess() {
		when(topicRepository.findById(idt2)).thenReturn(topic2);
		when(sessionRepository.findById(ids1)).thenReturn(session);
		service.addSessionToTopic(idt2, ids1);
		assertThat(topic2.getSessionList()).containsExactly(session2, session);
		verify(topicRepository, times(1)).update(topic2);
	}
	
	@Test
	public void testAddSessionToNullTopicFailure() {
		when(topicRepository.findById(idt1)).thenReturn(null);
		when(sessionRepository.findById(ids1)).thenReturn(session);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.addSessionToTopic(idt1, ids1));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
	}
	
	@Test
	public void testAddNullSessionToTopicFailure() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		when(sessionRepository.findById(ids1)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.addSessionToTopic(idt1, ids1));
		assertThat(e.getMessage()).isEqualTo("la sessione passata è null");
	}
	
	@Test
	public void testRemoveSessionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		when(sessionRepository.findById(ids1)).thenReturn(session);
		topic.setSessions(new ArrayList<StudySession>(List.of(session)));
		session.addTopic(topic2);
		service.removeSessionFromTopic(idt1, ids1);
		assertThat(topic.getSessionList()).isEmpty();
		verify(topicRepository, times(1)).update(topic);
	}
	
	@Test
	public void testRemoveSessionFromNullTopicFailure() {
		when(topicRepository.findById(idt1)).thenReturn(null);
		when(sessionRepository.findById(ids1)).thenReturn(session);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.removeSessionFromTopic(idt1, ids1));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
	}
	
	@Test
	public void testRemoveNullSessionFromTopicFailure() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		when(sessionRepository.findById(ids1)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.removeSessionFromTopic(idt1, ids1));
		assertThat(e.getMessage()).isEqualTo("la sessione passata è null");
	}
	
	@Test
	public void testTotalTimeWithSessionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		StudySession session1 = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		session1.setId(3L);
		topic.setSessions(new ArrayList<>(List.of(session,session1)));
		Integer result = service.calculateTotalTime(idt1);
		assertThat(result).isEqualTo(150);
		assertThat(topic.totalTime()).isEqualTo(150);
	}
	
	@Test
	public void testTotalTimeFailure() {
		when(topicRepository.findById(idt1)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.calculateTotalTime(idt1));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
	}
	
	@Test
	public void testPercentageOfCompletionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		StudySession session1 = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		session1.setId(3L);
		session1.setIsComplete(true);
		topic.setSessions(new ArrayList<>(List.of(session,session1)));
		Integer result = service.calculatePercentageOfCompletion(idt1);
		assertThat(result).isEqualTo(50);
		assertThat(topic.percentageOfCompletion()).isEqualTo(50);
	}
	
	@Test
	public void testPercentageOfCompletionFailure() {
		when(topicRepository.findById(idt1)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.calculatePercentageOfCompletion(idt1));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
	}
	
	
	
	
	
}
