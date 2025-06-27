package com.sessionbuilder.core;

public interface TransactionManager{
	<T> T doInTransaction(TransactionCode<T> code);
	<T> T doInSessionTransaction(StudySessionTransactionCode<T> code);
	<T> T doInTopicTransaction(TopicTransactionCode<T> code);
	<T> T doInMultiRepositoryTransaction(MultiRepositoryTransactionCode<T> code);
}
