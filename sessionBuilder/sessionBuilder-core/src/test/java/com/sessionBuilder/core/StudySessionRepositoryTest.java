package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.persistence.EntityManager;

@RunWith(MockitoJUnitRunner.class)
public class StudySessionRepositoryTest {
	
	
	@Mock
	private EntityManager em;
	
	@Mock
	private TransactionManager tm;
	
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
		when(em.find(StudySession.class, id)).thenReturn(session);
		StudySession result = sessionRepository.findById(id);
		assertThat(result).isEqualTo(session);
	}
	
	@Test
	public void testFindByIdFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> sessionRepository.findById(id));
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
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
