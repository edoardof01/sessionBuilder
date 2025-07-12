package com.sessionbuilder.core.backend;

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
	public StudySession createSession(LocalDate date, int duration, String note, List<Long> topicIds) {
		return tm.doInMultiRepositoryTransaction(context -> {
			if (context.getSessionRepository().findByDateDurationAndNote(date, duration, note) != null) {
				throw new IllegalArgumentException("esiste già una session con questi valori");
			}
			
			List<Topic> managedTopics = new ArrayList<>();
			if (topicIds != null) {
				for (Long topicId : topicIds) {
					Topic managedTopic = context.getTopicRepository().findById(topicId);
					managedTopics.add(managedTopic);
				}
			} else {
				throw new IllegalArgumentException("la session deve avere almeno un topic");
			}
			StudySession session = new StudySession(date, duration, note, managedTopics);
			context.getSessionRepository().save(session);
			
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
				topic.getSessionList().remove(session);
				context.getTopicRepository().update(topic);
			}
			context.getSessionRepository().delete(sessionId);
			return null;
		});
	}
}
