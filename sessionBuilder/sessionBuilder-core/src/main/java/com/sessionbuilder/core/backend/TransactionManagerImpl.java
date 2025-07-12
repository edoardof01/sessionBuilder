package com.sessionbuilder.core.backend;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import com.google.inject.Inject;
import java.util.function.Supplier;

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

	private <T> T executeInTransaction(Supplier<T> supplier) {
		if (emHolder.get() != null) {
			return supplier.get();
		}
		
		EntityManager em = emf.createEntityManager();
		emHolder.set(em);
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T result = supplier.get();
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
	public <T> T doInTransaction(TransactionCode<T> code) {
		return executeInTransaction(() -> code.apply(getCurrentEntityManager()));
	}

	@Override
	public <T> T doInTopicTransaction(TopicTransactionCode<T> code) {
		return executeInTransaction(() -> code.apply(topicRepository));
	}

	@Override
	public <T> T doInSessionTransaction(StudySessionTransactionCode<T> code) {
		return executeInTransaction(() -> code.apply(sessionRepository));
	}

	@Override
	public <T> T doInMultiRepositoryTransaction(MultiRepositoryTransactionCode<T> code) {
		return executeInTransaction(() -> {
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
			return code.apply(context);
		});
	}
}