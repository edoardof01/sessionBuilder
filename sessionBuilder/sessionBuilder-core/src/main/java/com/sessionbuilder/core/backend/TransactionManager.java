package com.sessionbuilder.core.backend;

import jakarta.persistence.EntityManager;

public interface TransactionManager{
	<T> T doInTransaction(TransactionCode<T> code);
	<T> T doInSessionTransaction(StudySessionTransactionCode<T> code);
	<T> T doInTopicTransaction(TopicTransactionCode<T> code);
	<T> T doInMultiRepositoryTransaction(MultiRepositoryTransactionCode<T> code);
	EntityManager getCurrentEntityManager();
	public ThreadLocal<EntityManager> getEmHolder();
}
