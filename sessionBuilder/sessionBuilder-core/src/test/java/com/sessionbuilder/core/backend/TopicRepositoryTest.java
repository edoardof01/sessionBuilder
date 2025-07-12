package com.sessionbuilder.core.backend;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		topic = new Topic();
		topic.setId(id);
		when(tm.getCurrentEntityManager()).thenReturn(em);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testFindByIdSuccess() {
		String jpql = "SELECT t FROM Topic t LEFT JOIN FETCH t.sessionList WHERE t.id = :id";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter("id", id)).thenReturn(typedQuery);
		when(typedQuery.getSingleResult()).thenReturn(topic);
		Topic result = topicRepository.findById(id);
		InOrder inOrder = Mockito.inOrder(em, typedQuery);
		inOrder.verify(em).createQuery(jpql, Topic.class);
		inOrder.verify(typedQuery).setParameter("id", id);
		inOrder.verify(typedQuery).getSingleResult();
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
	public void testFindByNameDescriptionAndDifficultyWhenTopicExistsShouldReturnTopic() {
		String name = "Biografie";
		String description = "Freddy Mercury";
		int difficulty = 2;
		Topic expectedTopic = new Topic(name, description, difficulty, new ArrayList<>());
		expectedTopic.setId(5L);
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(List.of(expectedTopic));
		ArgumentCaptor<String> paramNameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> paramValueCaptor = ArgumentCaptor.forClass(Object.class);
		when(typedQuery.setParameter(paramNameCaptor.capture(), paramValueCaptor.capture())).thenReturn(typedQuery);
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		InOrder inOrder = Mockito.inOrder(em, typedQuery);
		inOrder.verify(em).createQuery(jpql, Topic.class);
		inOrder.verify(typedQuery, times(3)).setParameter(anyString(), any());
		inOrder.verify(typedQuery).getResultList();
		List<String> capturedNames = paramNameCaptor.getAllValues();
		List<Object> capturedValues = paramValueCaptor.getAllValues();
		assertThat(capturedNames.get(0)).isEqualTo("name");
		assertThat(capturedValues.get(0)).isEqualTo(name);
		assertThat(capturedNames.get(1)).isEqualTo("description");
		assertThat(capturedValues.get(1)).isEqualTo(description);
		assertThat(capturedNames.get(2)).isEqualTo("difficulty");
		assertThat(capturedValues.get(2)).isEqualTo(difficulty);
		assertThat(result).isEqualTo(expectedTopic);
	}


	@Test
	public void testFindByNameDescriptionAndDifficultyWhenTopicDoesNotExistShouldReturnNull() {
		String name = "Biografie";
		String description = "Freddy Mercury";
		int difficulty = 2;
		String jpql = "SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty";
		when(em.createQuery(jpql, Topic.class)).thenReturn(typedQuery);
		when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
		Topic result = topicRepository.findByNameDescriptionAndDifficulty(name, description, difficulty);
		InOrder inOrder = Mockito.inOrder(em, typedQuery);
		inOrder.verify(em).createQuery(jpql, Topic.class);
		inOrder.verify(typedQuery, times(3)).setParameter(anyString(), any());
		inOrder.verify(typedQuery).getResultList();
		assertThat(result).isNull();
	}

	@Test
	public void testFindByNameDescriptionAndDifficultyWhenDatabaseErrorOccursShouldThrowException() {
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
