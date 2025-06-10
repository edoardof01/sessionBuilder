package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;

import com.google.inject.Inject;

public class StudySessionService implements StudySessionInterface {
	
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
	public StudySession createSession(LocalDate date, int duration, String note, ArrayList<Topic> topicList) {
		return tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = new StudySession(date, duration, note, topicList);
			sessionRepository.save(session);
			return session;
		});
	}
	
	@Override
	public void completeSession(long sessionId) {
		tm.doInSessionTransaction(sessionRepository ->{
			StudySession session = sessionRepository.findById(sessionId);
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			session.complete();
			sessionRepository.update(session);
			return null;
		});
	}
	
	@Override
	public void addTopic(long sessionId, long topicId) {
		tm.doInMultiRepositoryTransaction(context -> {
			StudySession session = context.getSessionRepository().findById(sessionId);
			Topic topic = context.getTopicRepository().findById(topicId);
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
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
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
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
