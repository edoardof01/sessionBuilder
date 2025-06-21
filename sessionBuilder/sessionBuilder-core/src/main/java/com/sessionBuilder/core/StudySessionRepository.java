package com.sessionBuilder.core;

import java.time.LocalDate;

import com.google.inject.Inject;

import jakarta.persistence.NoResultException;

public class StudySessionRepository implements StudySessionRepositoryInterface {
	

	private TransactionManager tm;
	
	@Inject
	public StudySessionRepository( TransactionManager tm) {
		 this.tm = tm;
	}

	@Override
	public StudySession findById(long id) {
		return tm.doInTransaction(em -> {
			try {
				return em.createQuery(
					"SELECT s FROM StudySession s LEFT JOIN FETCH s.topicList WHERE s.id = :id", 
					StudySession.class)
					.setParameter("id", id)
					.getSingleResult();
			} catch (NoResultException e) {
				throw new IllegalArgumentException("non esiste una session con tale id");
			}
		});
	}
	
	@Override 
	public StudySession findByDateDurationAndNote(LocalDate date, int duration, String note) {
		return tm.doInTransaction(em -> {
			try {
			return em.createQuery("SELECT s FROM StudySession s WHERE s.date = :date AND s.duration = :duration AND s.note = :note",
						StudySession.class)
				.setParameter("date", date)
				.setParameter("duration", duration)
				.setParameter("note", note)
				.getSingleResult();
			} catch (Exception e) {
				return null;
			}
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
