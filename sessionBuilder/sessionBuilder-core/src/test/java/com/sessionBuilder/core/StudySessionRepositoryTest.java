package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@RunWith(MockitoJUnitRunner.class)
public class StudySessionRepositoryTest {
	
	
	@Mock
	private EntityManager em;
	
	@Mock
	private TransactionManager tm;
	
	@Mock
	private TypedQuery<StudySession> typedQuery;
	
	@InjectMocks
	private StudySessionRepository sessionRepository;
	
	private final long id = 1L;
	private StudySession session;
	
	@Before
	public void setup() {
		when(tm.doInTransaction(any())).thenAnswer(answer -> {
			TransactionCode<?> code = answer.getArgument(0);
			return code.apply(em);
		});
		session = new StudySession();
		session.setId(id);
	}
	
	@Test
	public void testfindByIdSuccess() {
		String jpql = "SELECT s FROM StudySession s LEFT JOIN FETCH s.topicList WHERE s.id = :id";
		when(em.createQuery(jpql, StudySession.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter("id", id)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenReturn(session);
		StudySession result = sessionRepository.findById(id);
		assertThat(result).isEqualTo(session);
	}

	@Test
	public void testFindByIdFailure() {
		String jpql = "SELECT s FROM StudySession s LEFT JOIN FETCH s.topicList WHERE s.id = :id";
		when(em.createQuery(jpql, StudySession.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter("id", id)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> sessionRepository.findById(id));
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
	
	@Test
	public void testFindByDateDurationAndNoteSuccess() {
		LocalDate date = LocalDate.now().plusDays(1);
		int duration = 60;
		String note = "una nota";
		Topic topic = new Topic("Sport Acquatici", "kayak", 2, new ArrayList<>());
		long otherId = 5L;
		StudySession other = new StudySession(date, duration, note, new ArrayList<>(List.of(topic)));
		other.setId(otherId);
		String jpql = "SELECT s FROM StudySession s WHERE s.date = :date AND s.duration = :duration AND s.note = :note";
		when(em.createQuery(jpql, StudySession.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(List.of(other));
		StudySession result = sessionRepository.findByDateDurationAndNote(date, duration, note);
		assertThat(result).isEqualTo(other);
	}

	@Test
	public void testFindByDateDurationAndNoteNotFoundShouldReturnNull() {
		LocalDate date = LocalDate.now().plusDays(1);
		int duration = 60;
		String note = "una nota";
		String jpql = "SELECT s FROM StudySession s WHERE s.date = :date AND s.duration = :duration AND s.note = :note";
		when(em.createQuery(jpql, StudySession.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
		StudySession result = sessionRepository.findByDateDurationAndNote(date, duration, note);
		assertThat(result).isNull();
	}

	@Test
	public void testFindByDateDurationAndNoteWithExceptionShouldPropagate() {
		LocalDate date = LocalDate.now();
		int duration = 60;
		String note = "test";
		String jpql = "SELECT s FROM StudySession s WHERE s.date = :date AND s.duration = :duration AND s.note = :note";
		when(em.createQuery(jpql, StudySession.class)).thenThrow(new RuntimeException("Database error"));
		RuntimeException e = assertThrows(RuntimeException.class,
			() -> sessionRepository.findByDateDurationAndNote(date, duration, note));
		assertThat(e.getMessage()).isEqualTo("Database error");
	}

	@Test
	public void testFindAllSuccess() {
		Topic topic = new Topic();
		StudySession sessionA = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		StudySession sessionB = new StudySession(LocalDate.now().plusDays(2), 90, "un'altra nota", new ArrayList<>(List.of(topic)));
		List<StudySession> allSessions = new ArrayList<>(List.of(sessionA, sessionB));
		String jpql = "SELECT s FROM StudySession s";
		when(em.createQuery(jpql, StudySession.class)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(allSessions);
		List<StudySession> result = sessionRepository.findAll();
		assertThat(result).isEqualTo(allSessions);
	}
	
	@Test
	public void testFindAllFailure() {
		String jpql = "SELECT s FROM StudySession s";
		when(em.createQuery(jpql, StudySession.class)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenThrow(new IllegalArgumentException());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, 
	    		()-> sessionRepository.findAll());
		assertThat(e.getMessage()).isEqualTo("Errore nell'estrazione delle session");
	}

	@Test
	public void testSaveSuccess() {
		sessionRepository.save(session);
		assertThat(session).isNotNull();
		assertThat(session.getId()).isPositive();
		verify(em, times(1)).persist(session);
	}
	
	@Test
	public void testSaveFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> 	sessionRepository.save(null));
		assertThat(e.getMessage()).isEqualTo("la sessione da persistere è null");
	}
	
	@Test
	public void testUpdateSuccess() {
		sessionRepository.update(session);
		verify(em, times(1)).merge(session);
	}
	
	@Test
	public void testUpdateFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> 	sessionRepository.update(null));
		assertThat(e.getMessage()).isEqualTo("la sessione da aggiornare è null");
	}
	
	@Test
	public void testDeleteSuccess() {
		when(em.find(StudySession.class, id)).thenReturn(session);
		sessionRepository.delete(id);
		verify(em, times(1)).find(StudySession.class, id);
		verify(em, times(1)).remove(session);
	}
	
	@Test
	public void testDeleteFailure() {
		when(em.find(StudySession.class, id)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> sessionRepository.delete(id));
		assertThat(e.getMessage()).isEqualTo("la sessione da rimuovere non esiste");
	}
	
	

}
