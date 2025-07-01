package com.sessionbuilder.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import com.google.inject.Inject;

public class TransactionManagerImpl implements TransactionManager {

	private static final ThreadLocal<EntityManager> emHolder = new ThreadLocal<>();
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
	public EntityManager getCurrentEntityManager() {
		EntityManager em = emHolder.get();
		if (em == null) {
			 throw new IllegalStateException("EntityManager non trovato. La transazione non è stata avviata correttamente");
		}
		return em;
	}
	
	@Override
	public ThreadLocal<EntityManager> getEmHolder() {
		return emHolder;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		if(emHolder.get() != null) {
			return code.apply(getCurrentEntityManager());
		}
		EntityManager em = emf.createEntityManager();
		emHolder.set(em);
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
			emHolder.remove();
			em.close();
		}
	}

	@Override
	public <T> T doInTopicTransaction(TopicTransactionCode<T> code) {
		if(emHolder.get() != null) {
			return code.apply(topicRepository);
		}
		EntityManager em = emf.createEntityManager();
		emHolder.set(em);
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
			emHolder.remove();
			em.close();
		}
	}

	@Override
	public <T> T doInSessionTransaction(StudySessionTransactionCode<T> code) {
		if(emHolder.get() != null) {
			return code.apply(sessionRepository);
		}
		EntityManager em = emf.createEntityManager();
		emHolder.set(em);
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
			emHolder.remove();
			em.close();
		}
	}

	@Override
	public <T> T doInMultiRepositoryTransaction(MultiRepositoryTransactionCode<T> code) {
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
		if(emHolder.get() != null) {
			return code.apply(context);
		}
		EntityManager em = emf.createEntityManager();
		emHolder.set(em);
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T result = code.apply(context);
			transaction.commit();
			return result;
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			emHolder.remove();
			em.close();
		}
	}
}