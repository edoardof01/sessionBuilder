package com.sessionBuilder.core;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@RunWith(MockitoJUnitRunner.class)
public class TransactionManagerImplTest {
	
	@Mock
	private EntityManagerFactory emf;
	
	@Mock
	private TopicRepositoryInterface topicRepository;
	
	@Mock
	private StudySessionRepositoryInterface sessionRepository;
	
	@Mock
	private EntityManager em;
	
	@Mock
	private EntityTransaction transaction;
	
	private TransactionManagerImpl transactionManager;
	
	@Before
	public void setup() {
		transactionManager = new TransactionManagerImpl(emf, topicRepository, sessionRepository);
		when(emf.createEntityManager()).thenReturn(em);
		when(em.getTransaction()).thenReturn(transaction);
		when(transaction.isActive()).thenReturn(true);
	}
	
	@Test
	public void testDoInTransactionSuccess() {
		String expectedResult = "test result";
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		when(code.apply(em)).thenReturn(expectedResult);
		
		String result = transactionManager.doInTransaction(code);
		
		verify(transaction).begin();
		verify(code).apply(em);
		verify(transaction).commit();
		verify(em).close();
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testDoInTransactionWithException() {
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		RuntimeException exception = new RuntimeException("Test exception");
		when(code.apply(em)).thenThrow(exception);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testDoInTransactionWithInactiveTransaction() {
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		RuntimeException exception = new RuntimeException("Test exception");
		when(code.apply(em)).thenThrow(exception);
		when(transaction.isActive()).thenReturn(false);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction, never()).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testDoInTopicTransactionSuccess() {
		String expectedResult = "topic result";
		@SuppressWarnings("unchecked")
		TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
		when(code.apply(topicRepository)).thenReturn(expectedResult);
		
		String result = transactionManager.doInTopicTransaction(code);
		
		verify(transaction).begin();
		verify(code).apply(topicRepository);
		verify(transaction).commit();
		verify(em).close();
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testDoInTopicTransactionWithException() {
		@SuppressWarnings("unchecked")
		TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
		RuntimeException exception = new RuntimeException("Topic exception");
		when(code.apply(topicRepository)).thenThrow(exception);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInTopicTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testDoInSessionTransactionSuccess() {
		String expectedResult = "session result";
		@SuppressWarnings("unchecked")
		StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
		when(code.apply(sessionRepository)).thenReturn(expectedResult);
		
		String result = transactionManager.doInSessionTransaction(code);
		
		verify(transaction).begin();
		verify(code).apply(sessionRepository);
		verify(transaction).commit();
		verify(em).close();
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testDoInSessionTransactionWithException() {
		@SuppressWarnings("unchecked")
		StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
		RuntimeException exception = new RuntimeException("Session exception");
		when(code.apply(sessionRepository)).thenThrow(exception);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInSessionTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testDoInMultiRepositoryTransactionSuccess() {
		String expectedResult = "multi result";
		@SuppressWarnings("unchecked")
		MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
		when(code.apply(any(RepositoryContext.class))).thenReturn(expectedResult);
		
		String result = transactionManager.doInMultiRepositoryTransaction(code);
		
		verify(transaction).begin();
		verify(code).apply(any(RepositoryContext.class));
		verify(transaction).commit();
		verify(em).close();
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testDoInMultiRepositoryTransactionWithException() {
		@SuppressWarnings("unchecked")
		MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
		RuntimeException exception = new RuntimeException("Multi exception");
		when(code.apply(any(RepositoryContext.class))).thenThrow(exception);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInMultiRepositoryTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}

	@Test
	public void testDoInTopicTransactionWithInactiveTransaction() {
		@SuppressWarnings("unchecked")
		TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
		RuntimeException exception = new RuntimeException("Topic exception");
		when(code.apply(topicRepository)).thenThrow(exception);
		when(transaction.isActive()).thenReturn(false);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInTopicTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction, never()).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}

	@Test
	public void testDoInSessionTransactionWithInactiveTransaction() {
		@SuppressWarnings("unchecked")
		StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
		RuntimeException exception = new RuntimeException("Session exception");
		when(code.apply(sessionRepository)).thenThrow(exception);
		when(transaction.isActive()).thenReturn(false);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInSessionTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction, never()).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}

	@Test
	public void testDoInMultiRepositoryTransactionWithInactiveTransaction() {
		@SuppressWarnings("unchecked")
		MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
		RuntimeException exception = new RuntimeException("Multi exception");
		when(code.apply(any(RepositoryContext.class))).thenThrow(exception);
		when(transaction.isActive()).thenReturn(false);
		
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
			transactionManager.doInMultiRepositoryTransaction(code);
		});
		
		verify(transaction).begin();
		verify(transaction, never()).rollback();
		verify(em).close();
		assertThat(thrown).isEqualTo(exception);
	}
	
	@Test
	public void testRepositoryContextGetters() {
		MultiRepositoryTransactionCode<Void> code = context -> {
			assertThat(context.getTopicRepository()).isEqualTo(topicRepository);
			assertThat(context.getSessionRepository()).isEqualTo(sessionRepository);
			return null;
		};
		
		transactionManager.doInMultiRepositoryTransaction(code);
		
		verify(transaction).begin();
		verify(transaction).commit();
		verify(em).close();
	}
}