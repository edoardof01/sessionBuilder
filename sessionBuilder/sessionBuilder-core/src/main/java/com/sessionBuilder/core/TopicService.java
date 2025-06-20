package com.sessionBuilder.core;


import java.util.List;

import com.google.inject.Inject;

public class TopicService implements TopicServiceInterface {
	
	@Inject
	private TopicRepositoryInterface topicRepository;
	
	@Inject
	private TransactionManager tm;
	
	private static final String TOPIC_EXCEPTION_MESSAGE = "il topic passato è null";

	@Override
	public Topic createTopic(String name, String description, int difficulty, List<StudySession> sessionList) {
		return tm.doInTopicTransaction(repository -> {
			Topic topic = new Topic(name, description, difficulty, sessionList);
			if(repository.findByNameDescriptionAndDifficulty(name, description, difficulty) != null) 
				throw new IllegalArgumentException("Esiste già un topic con questi valori");
			topicRepository.save(topic);
			return topic;
		});
	}

	@Override
	public Topic getTopicById(long topicId) {
		return tm.doInTopicTransaction(repository -> {
			Topic topic = repository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException("il topic cercato non esiste");
			return topic;
		});
	}

	@Override
	public void addSessionToTopic(long topicId, long sessionId) {
		tm.doInMultiRepositoryTransaction(context -> {
			Topic topic = context.getTopicRepository().findById(topicId);
			StudySession session = context.getSessionRepository().findById(sessionId);
			if(topic == null) throw new IllegalArgumentException(TOPIC_EXCEPTION_MESSAGE);
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
			if(topic == null) throw new IllegalArgumentException(TOPIC_EXCEPTION_MESSAGE);
			if(session == null) throw new IllegalArgumentException("la sessione passata è null");
			topic.removeSession(session);
			context.getTopicRepository().update(topic);
			return null;
		});
	}
	
	@Override
	public void deleteTopic(long topicId) {
		tm.doInTopicTransaction(repository -> {
			Topic topic = repository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException(TOPIC_EXCEPTION_MESSAGE);
			repository.delete(topicId);
			return null;
		});
	}


	@Override
	public int calculateTotalTime(long topicId) {
		return tm.doInTopicTransaction(repository -> {
			Topic topic = repository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException(TOPIC_EXCEPTION_MESSAGE);
			return topic.totalTime();
		});
	}

	@Override
	public int calculatePercentageOfCompletion(long topicId) {
		return tm.doInTopicTransaction(repository -> {
			Topic topic = repository.findById(topicId);
			if(topic == null) throw new IllegalArgumentException(TOPIC_EXCEPTION_MESSAGE);
			return topic.percentageOfCompletion();
		});	
	}

	
	

}
