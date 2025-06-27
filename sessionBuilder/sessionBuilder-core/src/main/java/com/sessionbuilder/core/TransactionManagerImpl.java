package com.sessionbuilder.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import com.google.inject.Inject;

public class TransactionManagerImpl implements TransactionManager {
   
   private EntityManagerFactory emf;
   private TopicRepositoryInterface topicRepository;
   private StudySessionRepositoryInterface sessionRepository;
   @Inject
   public TransactionManagerImpl(EntityManagerFactory emf, 
   							TopicRepositoryInterface topicRepository,
   							StudySessionRepositoryInterface sessionRepository) {
   	this.emf = emf;
   	this.topicRepository = topicRepository;
   	this.sessionRepository = sessionRepository;
   } 
   @Override
   public <T> T doInTransaction(TransactionCode<T> code) {
   	EntityManager em = emf.createEntityManager();
   	EntityTransaction transaction = em.getTransaction();
   	try {
   		transaction.begin();
   		T result = code.apply(em);
   		transaction.commit();
   		return result;
   	} catch (Exception e) {
   		if (transaction.isActive()) {
   			transaction.rollback();
   		}
   		throw e;
   	} finally {
   		em.close();
   	}
   }
   
   @Override
   public <T> T doInTopicTransaction(TopicTransactionCode<T> code) {
   	EntityManager em = emf.createEntityManager();
   	EntityTransaction transaction = em.getTransaction();
   	try {
   		transaction.begin();
   		T result = code.apply(topicRepository);
   		transaction.commit();
   		return result;
   	} catch (Exception e) {
   		if (transaction.isActive()) {
   			transaction.rollback();
   		}
   		throw e;
   	} finally {
   		em.close();
   	}
   }
   
   @Override
   public <T> T doInSessionTransaction(StudySessionTransactionCode<T> code) {
   	EntityManager em = emf.createEntityManager();
   	EntityTransaction transaction = em.getTransaction();
   	try {
   		transaction.begin();
   		T result = code.apply(sessionRepository);
   		transaction.commit();
   		return result;
   	} catch (Exception e) {
   		if (transaction.isActive()) {
   			transaction.rollback();
   		}
   		throw e;
   	} finally {
   		em.close();
   	}
   }
   
   @Override
   public <T> T doInMultiRepositoryTransaction(MultiRepositoryTransactionCode<T> code) {
   	EntityManager em = emf.createEntityManager();
   	EntityTransaction transaction = em.getTransaction();
   	try {
   		transaction.begin();

   		RepositoryContext context = new RepositoryContext() {
   			@Override
   			public TopicRepositoryInterface getTopicRepository() {
   				return topicRepository;
   			}
   			
   			@Override
   			public StudySessionRepositoryInterface getSessionRepository() {
   				return sessionRepository;
   			}
   		};
   		
   		T result = code.apply(context);
   		transaction.commit();
   		return result;
   	} catch (Exception e) {
   		if (transaction.isActive()) {
   			transaction.rollback();
   		}
   		throw e;
   	} finally {
   		em.close();
   	}
   }
}