package com.sessionbuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

public class StudySessionService implements StudySessionInterface {
	
	private static final String NULL_SESSION_MESSAGE = "la sessione passata è null";
	@Inject
	private TransactionManager tm;

	public StudySession getSessionById(long id) {
		return tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(id);
			if(session == null) throw new IllegalArgumentException("non esiste una session con tale id");
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
		return tm.doInMultiRepositoryTransaction(context -> {
			StudySession session = new StudySession(date, duration, note, topicList);
			if(context.getSessionRepository().findByDateDurationAndNote(date, duration, note) != null) {
				throw new IllegalArgumentException("esiste già una session con questi valori");
			}
			context.getSessionRepository().save(session);
			for (Topic topic : topicList) {
				context.getTopicRepository().update(topic);
			}
			return session;
		});
	}
	
	@Override
	public StudySession completeSession(long sessionId) {
		return tm.doInMultiRepositoryTransaction(context ->{
			StudySession session = context.getSessionRepository().findById(sessionId);
			if(session == null) throw new IllegalArgumentException(NULL_SESSION_MESSAGE);
			session.complete();
			context.getSessionRepository().update(session);
			for (Topic topic : session.getTopicList()) {
				context.getTopicRepository().update(topic);
			}
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
			context.getTopicRepository().update(topic);
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
			context.getTopicRepository().update(topic);
			return null;
		});
	}
	
	@Override
	public void deleteSession(long sessionId) {
		tm.doInMultiRepositoryTransaction(context -> {
			StudySession session = context.getSessionRepository().findById(sessionId);
			if(session == null) throw new IllegalArgumentException("la sessione da rimuovere è null");
			List<Topic> topicsToRemoveFrom = new ArrayList<>(session.getTopicList());
			for (Topic topic : topicsToRemoveFrom) {
				topic.removeSession(session);
				context.getTopicRepository().update(topic);
			}
			context.getSessionRepository().delete(sessionId);
			return null;
		});
	}
}
