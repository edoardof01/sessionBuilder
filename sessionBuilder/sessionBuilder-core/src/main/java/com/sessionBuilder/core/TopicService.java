package com.sessionBuilder.core;

import java.util.ArrayList;

import com.google.inject.Inject;

public class TopicService implements TopicServiceInterface {
	
	@Inject
	private TopicRepositoryInterface topicRepository;
	
	@Inject
	private TransactionManager tm;

	@Override
	public Topic createTopic(String name, String description, int difficulty, ArrayList<StudySession> sessionList) {
		Topic topic = new Topic(name, description, difficulty, sessionList);
		topicRepository.save(topic);
		return topic;
	}

	@Override
	public Topic getTopicById(long topicId) {
		return tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic cercato non esiste");
			return topic;
		});
	}

	@Override
	public void addSessionToTopic(long topicId, long sessionId) {
		tm.doInMultiRepositoryTransaction(context -> {
			Topic topic = context.getTopicRepository().findById(topicId);
			StudySession session = context.getSessionRepository().findById(sessionId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			topic.addSession(session);
			context.getTopicRepository().update(topic);
			return null;
		});
	}
	
	@Override
	public void removeSessionFromTopic(long topicId, long sessionId) {
		tm.doInMultiRepositoryTransaction(context -> {
			Topic topic = context.getTopicRepository().findById(topicId);
			StudySession session = context.getSessionRepository().findById(sessionId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			topic.removeSession(session);
			context.getTopicRepository().update(topic);
			return null;
		});
	}
	
	@Override
	public void deleteTopic(long topicId) {
		tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			topicRepository.delete(topicId);
			return null;
		});
	}


	@Override
	public int calculateTotalTime(long topicId) {
		return tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			return topic.totalTime();
		});
	}

	@Override
	public int calculatePercentageOfCompletion(long topicId) {
		return tm.doInTopicTransaction(topicRepository -> {
			Topic topic = topicRepository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic passato è null");
			return topic.percentageOfCompletion();
		});	
	}

	
	

}
