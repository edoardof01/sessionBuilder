package com.sessionBuilder.core;

import com.google.inject.Inject;

public class StudySessionRepository implements StudySessionRepositoryInterface {
	

	private TransactionManager tm;
	
	@Inject
	public StudySessionRepository( TransactionManager tm) {
		 this.tm = tm;
	}

	@Override
	public StudySession findById(long id) {
		return tm.doInTransaction(em -> {
			StudySession session = em.find(StudySession.class, id);
			if(session == null) throw new IllegalArgumentException("non esiste una session con tale id");
			return session;
		});	
	}

	@Override
	public void save(StudySession session) {
		tm.doInTransaction(em -> {
			if(session == null) throw new IllegalArgumentException("la sessione da persistere è null");
			em.persist(session);
			return null;
		});
	}
	
	@Override
	public void update(StudySession session) {
		tm.doInTransaction(em -> {
			if(session == null) throw new IllegalArgumentException("la sessione da aggiornare è null");
			em.merge(session);
			return null;
		});
	}
	
	@Override
	public void delete(long id) {
		tm.doInTransaction(em -> {
			StudySession session = em.find(StudySession.class, id);
			if(session == null) throw new IllegalArgumentException("la sessione da rimuovere non esiste");
			em.remove(session);
			return null;
		});
	}
	


	

	
}
