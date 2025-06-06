package com.sessionBuilder.core;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopicControllerTest {
	
	@Mock
	private TopicService service;
	
	@InjectMocks
	private TopicController topicController;
	
	private Topic topic;
	private StudySession session;
	
	private final long idt1 = 1L;
	private final long ids1 = 2L;
	
	private String name;
	private String description;
	private int difficulty;
	
	
	@Before
	public void setup() {
		name = "Arredamento";
		description = "abbinamento materiali";
		difficulty = 4;
		topic = new Topic(name, description, difficulty, new ArrayList<>());
		session = new StudySession(LocalDate.now().plusDays(1), 90, "una nota", new ArrayList<>(List.of(topic)));
		session.setId(ids1);
	}
	
	@Test
	public void testHandleGetTopicSuccess() {
		when(service.getTopicById(idt1)).thenReturn(topic);
		Topic result = topicController.handleGetTopicById(idt1);
		verify(service).getTopicById(idt1);
		assertThat(result).isEqualTo(topic);
	}
	
	@Test
	public void testHandleCreateTopicSuccess() {
		when(service.createTopic(name, description, difficulty, new ArrayList<>())).thenReturn(topic);
		Topic result = topicController.handleCreateTopic(name, description, difficulty, new ArrayList<>());
		assertThat(result).isEqualTo(topic);
		verify(service).createTopic(name, description, difficulty, new ArrayList<>());
	}
	
	@Test 
	public void testHandleAddSessionSuccess() {
		topicController.handleAddSessionToTopic(idt1, ids1);
		verify(service).addSessionToTopic(idt1, ids1);
	}
	
	@Test
	public void testHandleTotalTime() {
		topicController.handleTotalTime(idt1);
		verify(service).calculateTotalTime(idt1);
	}

	@Test
	public void testPercentageOfCompletion() {
		topicController.handlePercentageOfCompletion(idt1);
		verify(service).calculatePercentageOfCompletion(idt1);
	}
	
	@Test
	public void testDeleteTopic() {
		topicController.handleDeleteTopic(idt1);
		verify(service).deleteTopic(idt1);
	}

	
	

}
