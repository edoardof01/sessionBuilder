package com.sessionBuilder.core;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;
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
	private StudySessionService service;
	
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
	public void testHandleCreateStudySessionSuccess() {
		when(service.createSession(date, duration, note, topics)).thenReturn(session);
		StudySession result = sessionController.handleCreateSession(date, duration, note, topics);
		verify(service).createSession(date, duration, note, topics);
		assertThat(result).isEqualTo(session);
	}
	
	@Test
	public void testHandleGetSessionSuccess() {
		when(service.getSessionById(ids1)).thenReturn(session);
		StudySession result = sessionController.handleGetSession(ids1);
		verify(service).getSessionById(ids1);
		assertThat(result).isEqualTo(session);
	}
	
	@Test
	public void testHandleAddTopicSuccess() {
		sessionController.handleAddTopic(ids1, idt2);
		verify(service).addTopic(ids1, idt2);
	}
	
	@Test
	public void testHandleRemoveTopicSuccess() {
		session.setTopics(new ArrayList<>(List.of(topic,topic1)));
		sessionController.handleRemoveTopic(ids1,idt2);
		verify(service).removeTopic(ids1, idt2);
	}
	
	@Test
	public void testHandleCompleteSession() {
		sessionController.handleCompleteSession(ids1);
		verify(service).completeSession(ids1);
	}
	
	@Test
	public void testHandleDeleteSession() {
		sessionController.handleDeleteSession(ids1);
		verify(service).deleteSession(ids1);
	}

}
