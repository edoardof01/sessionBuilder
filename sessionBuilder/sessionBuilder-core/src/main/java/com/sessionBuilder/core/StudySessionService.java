package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.List;

import com.google.inject.Inject;

public class StudySessionService implements StudySessionInterface {
	
	private static final String NULL_SESSION_MESSAGE = "la sessione passata è null";
	@Inject
	private TransactionManager tm;

	public StudySession getSessionById(long id) {
		return tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(id);
			if(session == null) throw new IllegalArgumentException("la sessione cercata non esiste");
			return session;
		});
	}
	
	@Override
	public List<StudySession> getAllSessions() {
		return tm.doInSessionTransaction(sessionRepository -> {
			try {
				return sessionRepository.findAll();
			} catch(Exception e) {
				throw new IllegalArgumentException("Errore durante il caricamento delle session");
			}
		});
	}

	@Override
	public StudySession createSession(LocalDate date, int duration, String note, List<Topic> topicList) {
		return tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = new StudySession(date, duration, note, topicList);
			if(sessionRepository.findByDateDurationAndNote(date, duration, note) != null) {
				throw new IllegalArgumentException("esiste già una session con questi valori");
			}
			sessionRepository.save(session);
			return session;
		});
	}
	
	@Override
	public StudySession completeSession(long sessionId) {
		return tm.doInSessionTransaction(sessionRepository ->{
			StudySession session = sessionRepository.findById(sessionId);
			if(session == null) throw new IllegalArgumentException(NULL_SESSION_MESSAGE);
			session.complete();
			sessionRepository.update(session);
			return session;
		});
	}
	
	@Override
	public void addTopic(long sessionId, long topicId) {
		tm.doInMultiRepositoryTransaction(context -> {
			StudySession session = context.getSessionRepository().findById(sessionId);
			Topic topic = context.getTopicRepository().findById(topicId);
			if(session == null) throw new IllegalArgumentException(NULL_SESSION_MESSAGE);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			session.addTopic(topic);
			context.getSessionRepository().update(session);
			return null;
		});
	}
	
	@Override
	public void removeTopic(long sessionId, long topicId) {
		tm.doInMultiRepositoryTransaction(context -> {
			StudySession session = context.getSessionRepository().findById(sessionId);
			Topic topic = context.getTopicRepository().findById(topicId);
			if(session == null) throw new IllegalArgumentException(NULL_SESSION_MESSAGE);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			session.removeTopic(topic);
			context.getSessionRepository().update(session);
			return null;
		});
	}
	
	@Override
	public void deleteSession(long sessionId) {
		tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(sessionId);
			if(session == null) throw new IllegalArgumentException("la sessione da rimuovere è null");
			sessionRepository.delete(sessionId);
			return null;
		});
		
	}

	
	
	
	
	
	
	
	
	

}
