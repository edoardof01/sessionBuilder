package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
	public void testFindByIdFailure() {
		String jpql = "SELECT t FROM Topic t LEFT JOIN FETCH t.sessionList WHERE t.id = :id";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter("id", id)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenThrow(new NoResultException());
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> topicRepository.findById(id));
		assertThat(e.getMessage()).isEqualTo("non esiste un topic con tale id");
	}
	
	@Test
	public void testFindByNameDescriptionAndDifficulty_WhenTopicExists_ShouldReturnTopic() {
		String name = "Biografie";
		String description = "Freddy Mercury";
		int difficulty = 2;
		Topic expectedTopic = new Topic(name, description, difficulty, new ArrayList<>());
		expectedTopic.setId(5L);
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(List.of(expectedTopic));
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		assertThat(result).isEqualTo(expectedTopic);
	}

	@Test
	public void testFindByNameDescriptionAndDifficulty_WhenTopicDoesNotExist_ShouldReturnNull() {
		String name = "Biografie";
		String description = "Freddy Mercury";
		int difficulty = 2;
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		assertThat(result).isNull();
	}

	@Test
	public void testFindByNameDescriptionAndDifficulty_WhenDatabaseErrorOccurs_ShouldThrowException() {
		String name = "Test Topic";
		String description = "Test Description";
		int difficulty = 3;
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenThrow(new RuntimeException("Database error"));
		RuntimeException e = assertThrows(RuntimeException.class,
			() -> topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty));
		assertThat(e.getMessage()).isEqualTo("Database error");
	}
	
	@Test
	public void testFindAllSuccess() {
		Topic topicA = new Topic();
		Topic topicB = new Topic();
		List<Topic> allTopics = new ArrayList<>(List.of(topicA, topicB));
		String jpql = "SELECT t FROM Topic t";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(allTopics);
		List<Topic> result = topicRepository.findAll();
		assertThat(result).isEqualTo(allTopics);
	}
	
	@Test
	public void testFindAllFailure() {
		String jpql = "SELECT t FROM Topic t";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenThrow(new RuntimeException("Errore nel caricamento dei topic"));
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> topicRepository.findAll());
		assertThat(e.getMessage()).isEqualTo("erorre nell'estrazione dei topic");
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
