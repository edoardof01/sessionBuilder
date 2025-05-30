package com.sessionBuilder.core;

public interface TransactionManager{
	<T> T doInTransaction(TransactionCode<T> code);
	<T> T doInSessionTransaction(StudySessionTransactionCode<T> code);
	<T> T doInTopicTransaction(TopicTransactionCode<T> code);
}
