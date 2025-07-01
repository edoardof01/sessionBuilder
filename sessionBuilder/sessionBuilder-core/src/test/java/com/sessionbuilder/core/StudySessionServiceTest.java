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
public class StudySessionServiceTest {
	
	@Mock
	private StudySessionRepositoryInterface sessionRepository;
	
	@Mock
	private TopicRepositoryInterface topicRepository;
	
	@Mock
	private TransactionManager tm;
	
	@InjectMocks
	private StudySessionService service;
	
	
	private final long ids1 = 1L;
	private final long ids2 = 2L;
	private final long idt1 = 3L;
	private final long idt2 = 4L;
	private StudySession session1;
	private StudySession fullSession;
	private Topic topic1;
	private Topic topic2;
	
	private LocalDate date;
	private int duration;
	private String note;
	private ArrayList<Long> topicIds;
	
	@Before
	public void setup() {
		when(tm.doInSessionTransaction(any())).thenAnswer(answer -> {
			StudySessionTransactionCode<?> code = answer.getArgument(0);
			return code.apply(sessionRepository);
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
		session1 = new StudySession();
		session1.setId(ids1);
		topic1 = new Topic("tennis", "dritto e rovescio", 2, new ArrayList<>());
		topic1.setId(idt1);
		topic2 = new Topic("pasticceria", "dolci classici", 3, new ArrayList<>());
		topic2.setId(idt2);
		fullSession = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic1,topic2)));
		fullSession.setId(ids2);
		date = LocalDate.now().plusDays(1);
		duration = 60;
		note = "una nota";
		new ArrayList<>(List.of(topic1,topic2));
		topicIds = new ArrayList<>(List.of(topic1.getId(),topic2.getId()));
	}
	
	@Test
	public void testGetSessionByIdSuccess() {
		when(sessionRepository.findById(ids1)).thenReturn(session1);
		StudySession result = service.getSessionById(ids1);
		assertThat(result).isEqualTo(session1);
	}
	
	@Test
	public void testGetSessionByIdFailure() {
		when(sessionRepository.findById(ids1)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.getSessionById(ids1));
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
	
	@Test
	public void testGetAllSessionSuccess() {
		List<StudySession> allSessions = new ArrayList<>(List.of(session1, fullSession));
		when(sessionRepository.findAll()).thenReturn(allSessions);
		List<StudySession> result = service.getAllSessions();
		assertThat(result).isEqualTo(allSessions);
		verify(sessionRepository).findAll();
	}
	
	@Test
	public void testGetAllSessionFailure() {
		when(sessionRepository.findAll()).thenThrow(new IllegalArgumentException());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.getAllSessions());
		assertThat(e.getMessage()).isEqualTo("Errore durante il caricamento delle session");
	}
	
	@Test
	public void testCreateSessionSuccess() {
		when(topicRepository.findById(idt1)).thenReturn(topic1);
		when(topicRepository.findById(idt2)).thenReturn(topic2);
		StudySession session = service.createSession(date, duration, note, topicIds);
		verify(sessionRepository,times(1)).save(session);
		assertThat(session).isNotNull();
		assertThat(session.getDate()).isEqualTo(date);
		assertThat(session.getDuration()).isEqualTo(duration);
		assertThat(session.getTopicList()).containsExactlyInAnyOrder(topic1, topic2);
	}
	
	@Test
	public void testCreateSessionWithoutTopicsThrowsException() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.createSession(date, duration, note, null));
		assertThat(e.getMessage()).isEqualTo("la session deve avere almeno un topic");
	}
	
	@Test
	public void testCreateSessionWithDuplicatedValuesFailure() {
		StudySession other = new StudySession(date, duration, note, new ArrayList<>(List.of(topic1)));
		when(sessionRepository.findByDateDurationAndNote(date, duration, note)).thenReturn(other);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> 
			service.createSession(date, duration, note, topicIds));
		assertThat(e.getMessage()).isEqualTo("esiste già una session con questi valori");
	}
	
	
	@Test
	public void testCompleteNullSessionFailure() {
		when(sessionRepository.findById(10L)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.completeSession(10L));
		assertThat(e.getMessage()).isEqualTo("la sessione passata è null");
	}
	
	@Test
	public void testCompleteSessionSuccess() {
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		StudySession result = service.completeSession(ids2);
		verify(sessionRepository, times(1)).update(fullSession);
		assertThat(result).isEqualTo(fullSession);
		assertThat(fullSession.isComplete()).isTrue();
		assertThat(result.isComplete()).isTrue();
	}
	
	@Test
	public void testCompleteCompletedSessionFailure() {
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		fullSession.setIsComplete(true);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.completeSession(ids2));
		assertThat(e.getMessage()).isEqualTo("la sessione è già stata completata");
	}
	
	@Test
	public void testAddTopicSucess() {
		Topic topic3 = new Topic();
		topic3.setId(6L);
		assertThat(fullSession.getTopicList()).containsExactly(topic1, topic2);
		
		when(topicRepository.findById(6L)).thenReturn(topic3);
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		service.addTopic(ids2, 6L);
		assertThat(fullSession.getTopicList()).hasSize(3);
		assertThat(fullSession.getTopicList()).containsExactly(topic1, topic2, topic3);
		verify(sessionRepository, times(1)).update(fullSession);
		verify(topicRepository, times(1)).update(topic3);
	}
	
	@Test
	public void testAddTopicNullSessionFailure() {
		Topic topic3 = new Topic();
		topic3.setId(6L);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.addTopic(10L, 6L));
		assertThat(e.getMessage()).isEqualTo("la sessione passata è null");
	}
	
	@Test
	public void testAddTopicNullTopicFailure() {
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.addTopic(ids2, 10L));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
		assertThat(fullSession.getTopicList()).containsExactly(topic1, topic2);
	}
	
	@Test
	public void testRemoveTopicSuccess() {
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		when(topicRepository.findById(idt2)).thenReturn(topic2);
		service.removeTopic(ids2, idt2);
		assertThat(fullSession.getTopicList()).containsExactly(topic1);
		verify(sessionRepository, times(1)).update(fullSession);
		verify(topicRepository, times(1)).update(topic2);
	}
	
	@Test 
	public void testRemoveTopicNulSessionFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.removeTopic(10L, idt2));
		assertThat(e.getMessage()).isEqualTo("la sessione passata è null");
	}
	
	@Test
	public void testRemoveTopicNullTopicFailure() {
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.removeTopic(ids2, 10L));
		assertThat(e.getMessage()).isEqualTo("il topic passato è null");
	}
	
	@Test
	public void testDeleteSessionSuccess() {
		when(sessionRepository.findById(ids2)).thenReturn(fullSession);
		service.deleteSession(ids2);
		verify(sessionRepository, times(1)).delete(ids2);
	}
	
	@Test
	public void testDeleteNullSessionFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> service.deleteSession(10L));
		assertThat(e.getMessage()).isEqualTo("la sessione da rimuovere è null");
		verify(sessionRepository, times(0)).delete(10L);
	}
	
	@Test
	public void testDeleteSessionVerifyTopicRemoveSessionCall() {
		StudySession sessionWithTwoTopics = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", 
			new ArrayList<>(List.of(topic1, topic2)));
		sessionWithTwoTopics.setId(3L);
		when(sessionRepository.findById(3L)).thenReturn(sessionWithTwoTopics);
		service.deleteSession(3L);
		assertThat(topic1.getSessionList()).doesNotContain(sessionWithTwoTopics);
		verify(sessionRepository, times(1)).delete(3L);
		verify(topicRepository, times(2)).update(any(Topic.class));
	}
	
	@Test
	public void testCreateSessionWithEmptyTopicListStillUpdatesCorrectly() {
		ArrayList<Long> emptyTopicList = new ArrayList<>();
		StudySession session = service.createSession(date, duration, note, emptyTopicList);
		verify(sessionRepository, times(1)).save(session);
		verify(topicRepository, times(0)).update(any(Topic.class));
	}
	
	@Test
	public void testCompleteSessionWithSingleTopicUpdatesCorrectly() {
		StudySession singleTopicSession = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", 
			new ArrayList<>(List.of(topic1)));
		singleTopicSession.setId(3L);
		when(sessionRepository.findById(3L)).thenReturn(singleTopicSession);
		StudySession result = service.completeSession(3L);
		verify(sessionRepository, times(1)).update(singleTopicSession);
		assertThat(result.isComplete()).isTrue();
	}
}