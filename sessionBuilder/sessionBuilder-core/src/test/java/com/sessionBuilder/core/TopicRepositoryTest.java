package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

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
public class TopicRepositoryTest {
	
	@Mock
	private EntityManager em;
	
	@Mock
	private TransactionManager tm;
	
	@Mock
	private TypedQuery<Topic> typedQuery;
	
	@InjectMocks
	private TopicRepository topicRepository;
	
	private final long id = 1L;
	private Topic topic;
	
	@Before
	public void setup() {
		topic = new Topic();
		topic.setId(id);
		when(tm.doInTransaction(any())).thenAnswer(answer -> {
			TransactionCode<?> code = answer.getArgument(0);
			return code.apply(em);
		});
	}
	
	@Test
	public void testFindByIdSuccess() {
		String jpql = "SELECT t FROM Topic t LEFT JOIN FETCH t.sessionList WHERE t.id = :id";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter("id", id)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenReturn(topic);
		Topic result = topicRepository.findById(id);
		assertThat(result).isEqualTo(topic);
	}

	@Test
	public void findByIdFailure() {
		String jpql = "SELECT t FROM Topic t LEFT JOIN FETCH t.sessionList WHERE t.id = :id";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter("id", id)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> topicRepository.findById(id));
		assertThat(e.getMessage()).isEqualTo("non esiste un topic con tale id");
	}
	
	@Test
	public void findByNameDescriptionDifficultySuccess() {
		String name = "Biografie";
		String description = "Freddy Mercury";
		int difficulty = 2;
		long idOther = 5L;
		Topic other = new Topic(name, description, difficulty, new ArrayList<>());
		other.setId(idOther);
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenReturn(other);
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		assertThat(result).isEqualTo(other);
	}
	
	@Test
	public void findByNameDescriptionDifficultyFailure() {
		String name = "Biografie";
		String description = "Freddy Mercury";
		int difficulty = 2;
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenReturn(null);
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		assertThat(result).isNull();
	}
	
	@Test
	public void testFindByNameDescriptionAndDifficultyWithException() {
		String name = "Test Topic";
		String description = "Test Description";
		int difficulty = 3;
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		
		when(em.createQuery(jpql, Topic.class)).thenThrow(new RuntimeException("Database error"));
		
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		assertThat(result).isNull();
	}
	
	@Test
	public void testSaveSuccess() {
		topicRepository.save(topic);
		verify(em, times(1)).persist(topic);
	}
	
	@Test
	public void testSaveFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> topicRepository.save(null));
		assertThat(e.getMessage()).isEqualTo("il topic da persistere è null");
	}
	
	@Test
	public void testUpdateSuccess() {
		topicRepository.update(topic);
		verify(em, times(1)).merge(topic);
	}
	
	@Test
	public void testUpdateFailure() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> topicRepository.update(null));
		assertThat(e.getMessage()).isEqualTo("il topic da aggiornare è null");
	}
	
	@Test
	public void testDeleteSuccess() {
		when(em.find(Topic.class,id)).thenReturn(topic);
		topicRepository.delete(id);
		verify(em, times(1)).find(Topic.class, id);
		verify(em, times(1)).remove(topic);
	}
	
	@Test
	public void testDeleteFailure() {
		when(em.find(Topic.class, id)).thenReturn(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> topicRepository.delete(id));
		assertThat(e.getMessage()).isEqualTo("il topic da rimuovere è null");
	}

}
