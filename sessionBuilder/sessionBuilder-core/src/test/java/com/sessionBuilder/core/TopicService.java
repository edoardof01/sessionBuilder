package com.sessionBuilder.core;

import java.util.ArrayList;

import com.google.inject.Inject;

public class TopicService {
	
	@Inject
	private TopicRepositoryInterface topicRepository;
	
	@Inject
	private StudySessionRepositoryInterface sessionRepository;
	
	@Inject
	private TransactionManager tm;

	public Topic createTopic(String name, String description, int difficulty, ArrayList<StudySession> sessionList) {
		Topic topic = new Topic(name, description, difficulty, sessionList);
		topicRepository.save(topic);
		return topic;
	}

	public Topic getTopicById(long topicId) {
		return tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("la sessione cercata non esiste");
			return topic;
		});
	}

	public void addSessionToTopic(long topicId, long sessionId) {
		tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			StudySession session = sessionRepository.findById(sessionId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			topic.addSession(session);
			topicRepository.update(topic);
			return null;
		});
		
	}

	public void removeSessionFromTopic(long topicId, long sessionId) {
		tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			StudySession session = sessionRepository.findById(sessionId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			topic.removeSession(session);
			topicRepository.update(topic);
			return null;
		});
	}

	public void calculateTotalTime(long topicId) {
		tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			topic.totalTime();
			return null;
		});
		
	}

	public void calculatePercentageOfCompletion(long topicId) {
		tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			topic.percentageOfCompletion();
			return null;
		});
		
	}

	
	

}
