package com.sessionbuilder.core.backend;

import java.time.LocalDate;
import java.util.List;
import com.google.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class StudySessionRepository implements StudySessionRepositoryInterface {

	private TransactionManager tm;
	
	@Inject
	public StudySessionRepository(TransactionManager tm) {
		this.tm = tm;
	}

	@Override
	public StudySession findById(long id) {
		EntityManager em = tm.getCurrentEntityManager();
		try {
			return em.createQuery(
				"SELECT s FROM StudySession s LEFT JOIN FETCH s.topicList WHERE s.id = :id", 
				StudySession.class)
				.setParameter("id", id)
				.getSingleResult();
		} catch (NoResultException e) {
			throw new IllegalArgumentException("non esiste una session con tale id");
		}
	}
	
	@Override
	public StudySession findByDateDurationAndNote(LocalDate date, int duration, String note) {
		EntityManager em = tm.getCurrentEntityManager();
		List<StudySession> sessions = em.createQuery("SELECT s FROM StudySession s WHERE s.date = :date AND s.duration = :duration AND s.note = :note",
				StudySession.class)
			.setParameter("date", date)
			.setParameter("duration", duration)
			.setParameter("note", note)
			.getResultList();
		if (sessions.isEmpty()) {
			return null;
		} else {
			return sessions.get(0);
		}
	}
	
	@Override
	public List<StudySession> findAll() {
		EntityManager em = tm.getCurrentEntityManager();
		try {
			return em.createQuery("SELECT s FROM StudySession s", StudySession.class).getResultList();
		} catch (Exception e) {
			throw new IllegalArgumentException("Errore nell'estrazione delle session");
		}
	}
	
	@Override
	public void save(StudySession session) {
		EntityManager em = tm.getCurrentEntityManager();
		if(session == null) {
			throw new IllegalArgumentException("la sessione da persistere è null");
		}
		em.persist(session);
	}
	
	@Override
	public void update(StudySession session) {
		EntityManager em = tm.getCurrentEntityManager();
		if(session == null) {
			throw new IllegalArgumentException("la sessione da aggiornare è null");
		}
		em.merge(session);
	}
	
	@Override
	public void delete(long id) {
		EntityManager em = tm.getCurrentEntityManager();
		StudySession session = em.find(StudySession.class, id);
		if(session == null) {
			throw new IllegalArgumentException("la sessione da rimuovere non esiste");
		}
		em.remove(session);
	}
}