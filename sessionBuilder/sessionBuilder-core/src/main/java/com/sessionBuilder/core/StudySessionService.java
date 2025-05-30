package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;

import com.google.inject.Inject;

public class StudySessionService {
	
	@Inject
	private StudySessionRepositoryInterface sessionRepository;
	
	@Inject
	private TopicRepositoryInterface topicRepository;
	
	@Inject
	private TransactionManager tm;

	public StudySession getSessionById(long id) {
		return tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(id);
			if(session == null) throw new IllegalArgumentException("la sessione cercata non esiste");
			return session;
		});
	}

	public StudySession createSession(LocalDate date, int duration, String note, ArrayList<Topic> topicList) {
		return tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = new StudySession(date, duration, note, topicList);
			sessionRepository.save(session);
			return session;
		});
	}
	
	public void completeSession(long sessionId) {
		tm.doInSessionTransaction(sessionRepository ->{
			StudySession session = sessionRepository.findById(sessionId);
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			session.complete();
			sessionRepository.update(session);
			return null;
		});
	}
	
	public void addTopic(long sessionId, long topicId) {
		tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(sessionId);
			Topic topic = topicRepository.findById(topicId);
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			session.addTopic(topic);
			sessionRepository.update(session);
			return null;
		});
	}

	public void removeTopic(long sessionId, long topicId) {
		tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(sessionId);
			Topic topic = topicRepository.findById(topicId);
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			session.removeTopic(topic);
			sessionRepository.update(session);
			return null;
		});
	}
	
	public void deleteSession(long sessionId) {
		tm.doInSessionTransaction(sessionRepository -> {
			StudySession session = sessionRepository.findById(sessionId);
			if(session == null) throw new IllegalArgumentException("la sessione da rimuovere è null");
			sessionRepository.delete(sessionId);
			return null;
		});
		
	}
	
	
	
	
	
	
	
	

}
