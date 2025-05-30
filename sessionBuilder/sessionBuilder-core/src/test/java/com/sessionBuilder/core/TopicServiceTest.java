package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
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
	private Topic topic;
	private StudySession session;
	private final long ids1 = 2L;
	
	@Before
	public void setup() {
		when(tm.doInTopicTransaction(any())).thenAnswer(answer -> {
			TopicTransactionCode<?> code = answer.getArgument(0);
			return code.apply(topicRepository);
		});
		name = "Cucina";
		description = "Pasticceria";
		difficulty = 3;
		topic = new Topic(name, description, difficulty, new ArrayList<>());
		topic.setId(idt1);
		session = new StudySession(LocalDate.now().plusDays(2), 90, "una nota", new ArrayList<>(List.of(topic)));
		session.setId(ids1);
	}
	
	@Test
	public void getTopicById(){
		when(topicRepository.findById(idt1)).thenReturn(topic);
		Topic result = service.getTopicById(idt1);
		assertThat(result.equals(topic)).isTrue();
	}
	
	@Test
	public void testCreateTopicSuccess() {
		Topic topic = service.createTopic(name, description, difficulty, new ArrayList<>());
		verify(topicRepository,times(1)).save(topic);
		assertThat(topic).isNotNull();
		assertThat(topic.getName()).isEqualTo(name);
		assertThat(topic.getDescription()).isEqualTo(description);
		assertThat(topic.getDifficulty()).isEqualTo(difficulty);
	}
	
	@Test
	public void testAddSessionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		when(sessionRepository.findById(ids1)).thenReturn(session);
		service.addSessionToTopic(idt1, ids1);
		assertThat(topic.getSessionList()).containsExactly(session);
		verify(topicRepository, times(1)).update(topic);
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
		service.removeSessionFromTopic(idt1, ids1);
		assertThat(topic.getSessionList()).isEmpty();;
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
		service.calculateTotalTime(idt1);
		assertThat(topic.totalTime()).isEqualTo(150);
	}
	
	@Test
	public void testPercentageOfCompletionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		StudySession session1 = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		session1.setId(3L);
		session1.setIsComplete(true);
		topic.setSessions(new ArrayList<>(List.of(session,session1)));
		service.calculatePercentageOfCompletion(idt1);
		assertThat(topic.percentageOfCompletion()).isEqualTo(50);
	}
	
	@Test
	public void testZeroPercentageOfCompletionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic);
		service.calculatePercentageOfCompletion(idt1);
		assertThat(topic.percentageOfCompletion()).isEqualTo(0);
	}
	
	
	
}
