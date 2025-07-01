package com.sessionbuilder.core;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import org.junit.After;
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
	
	@After
	public void tearDown() {
		transactionManager.getEmHolder().remove();
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
	
	
	@Test
	public void testGetCurrentEntityManagerWhenThreadLocalIsNull() {
	    IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
	        transactionManager.getCurrentEntityManager();
	    });
	    assertThat(thrown.getMessage()).contains("EntityManager non trovato");
	}

	@Test
	public void testGetCurrentEntityManagerWhenThreadLocalHasValue() {
	    transactionManager.getEmHolder().set(em);
	    EntityManager result = transactionManager.getCurrentEntityManager();
	    assertThat(result).isEqualTo(em);
	}

	@Test
	public void testDoInTransactionWhenEntityManagerAlreadyExists() {
	    transactionManager.getEmHolder().set(em);
	    String expectedResult = "existing em result";
	    @SuppressWarnings("unchecked")
	    TransactionCode<String> code = mock(TransactionCode.class);
	    when(code.apply(em)).thenReturn(expectedResult);
	    String result = transactionManager.doInTransaction(code);
	    verify(transaction, never()).begin();
	    verify(code).apply(em);
	    verify(transaction, never()).commit();
	    verify(em, never()).close();
	    assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void testDoInTopicTransactionWhenEntityManagerAlreadyExists() {
	    transactionManager.getEmHolder().set(em);
	    String expectedResult = "existing em topic result";
	    @SuppressWarnings("unchecked")
	    TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
	    when(code.apply(topicRepository)).thenReturn(expectedResult);
	    String result = transactionManager.doInTopicTransaction(code);
	    verify(transaction, never()).begin();
	    verify(code).apply(topicRepository);
	    verify(transaction, never()).commit();
	    verify(em, never()).close();
	    assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void testDoInSessionTransactionWhenEntityManagerAlreadyExists() {
	    transactionManager.getEmHolder().set(em);
	    String expectedResult = "existing em session result";
	    @SuppressWarnings("unchecked")
	    StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
	    when(code.apply(sessionRepository)).thenReturn(expectedResult);
	    String result = transactionManager.doInSessionTransaction(code);
	    verify(transaction, never()).begin();
	    verify(code).apply(sessionRepository);
	    verify(transaction, never()).commit();
	    verify(em, never()).close();
	    assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void testDoInMultiRepositoryTransactionWhenEntityManagerAlreadyExists() {
	    transactionManager.getEmHolder().set(em);
	    String expectedResult = "existing em multi result";
	    @SuppressWarnings("unchecked")
	    MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
	    when(code.apply(any(RepositoryContext.class))).thenReturn(expectedResult);
	    String result = transactionManager.doInMultiRepositoryTransaction(code);
	    verify(transaction, never()).begin();
	    verify(code).apply(any(RepositoryContext.class));
	    verify(transaction, never()).commit();
	    verify(em, never()).close();
	    assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	public void testDoInMultiRepositoryTransactionMissingEmHolderRemove() {
	    @SuppressWarnings("unchecked")
	    MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
	    when(code.apply(any(RepositoryContext.class))).thenReturn("result");
	    transactionManager.doInMultiRepositoryTransaction(code);
	    assertThat(transactionManager.getEmHolder().get()).isNull();
	}
	
	@Test
	public void testThreadLocalIsSetDuringTransactionExecution() {
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		when(code.apply(any(EntityManager.class))).thenAnswer(invocation -> {
			assertThat(transactionManager.getEmHolder().get()).isNotNull();
			return "result";
		});
		transactionManager.doInTransaction(code);
	}

	@Test
	public void testThreadLocalIsSetDuringTopicTransactionExecution() {
		@SuppressWarnings("unchecked")
		TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
		when(code.apply(any(TopicRepositoryInterface.class))).thenAnswer(invocation -> {
			assertThat(transactionManager.getEmHolder().get()).isNotNull();
			return "result";
		});
		transactionManager.doInTopicTransaction(code);
	}

	@Test
	public void testThreadLocalIsSetDuringSessionTransactionExecution() {
		@SuppressWarnings("unchecked")
		StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
		when(code.apply(any(StudySessionRepositoryInterface.class))).thenAnswer(invocation -> {
			assertThat(transactionManager.getEmHolder().get()).isNotNull();
			return "result";
		});
		transactionManager.doInSessionTransaction(code);
	}

	@Test
	public void testThreadLocalIsSetDuringMultiRepositoryTransactionExecution() {
		@SuppressWarnings("unchecked")
		MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
		when(code.apply(any(RepositoryContext.class))).thenAnswer(invocation -> {
			assertThat(transactionManager.getEmHolder().get()).isNotNull();
			return "result";
		});
		transactionManager.doInMultiRepositoryTransaction(code);
	}

	@Test
	public void testThreadLocalIsCleanedAfterTransactionSuccess() {
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		when(code.apply(em)).thenReturn("result");
		transactionManager.doInTransaction(code);
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}

	@Test
	public void testThreadLocalIsCleanedAfterTopicTransactionSuccess() {
		@SuppressWarnings("unchecked")
		TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
		when(code.apply(topicRepository)).thenReturn("result");
		transactionManager.doInTopicTransaction(code);
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}

	@Test
	public void testThreadLocalIsCleanedAfterSessionTransactionSuccess() {
		@SuppressWarnings("unchecked")
		StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
		when(code.apply(sessionRepository)).thenReturn("result");
		transactionManager.doInSessionTransaction(code);
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}

	@Test
	public void testThreadLocalIsCleanedAfterTransactionException() {
		@SuppressWarnings("unchecked")
		TransactionCode<String> code = mock(TransactionCode.class);
		when(code.apply(em)).thenThrow(new RuntimeException("test"));
		assertThrows(RuntimeException.class, () -> {
			transactionManager.doInTransaction(code);
		});
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}

	@Test
	public void testThreadLocalIsCleanedAfterTopicTransactionException() {
		@SuppressWarnings("unchecked")
		TopicTransactionCode<String> code = mock(TopicTransactionCode.class);
		when(code.apply(topicRepository)).thenThrow(new RuntimeException("test"));
		assertThrows(RuntimeException.class, () -> {
			transactionManager.doInTopicTransaction(code);
		});
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}

	@Test
	public void testThreadLocalIsCleanedAfterSessionTransactionException() {
		@SuppressWarnings("unchecked")
		StudySessionTransactionCode<String> code = mock(StudySessionTransactionCode.class);
		when(code.apply(sessionRepository)).thenThrow(new RuntimeException("test"));
		assertThrows(RuntimeException.class, () -> {
			transactionManager.doInSessionTransaction(code);
		});
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}

	@Test
	public void testThreadLocalIsCleanedAfterMultiRepositoryTransactionException() {
		@SuppressWarnings("unchecked")
		MultiRepositoryTransactionCode<String> code = mock(MultiRepositoryTransactionCode.class);
		when(code.apply(any(RepositoryContext.class))).thenThrow(new RuntimeException("test"));
		assertThrows(RuntimeException.class, () -> {
			transactionManager.doInMultiRepositoryTransaction(code);
		});
		assertThat(transactionManager.getEmHolder().get()).isNull();
	}
}