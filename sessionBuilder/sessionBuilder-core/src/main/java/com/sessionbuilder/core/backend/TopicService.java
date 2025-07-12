package com.sessionbuilder.core.backend;


import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

public class TopicService implements TopicServiceInterface {
	
	@Inject
	private TransactionManager tm;
	
	private static final String TOPIC_EXCEPTION_MESSAGE = "il topic passato è null";

	@Override
	public Topic createTopic(String name, String description, int difficulty, List<Long> sessionIds) {
	    return tm.doInMultiRepositoryTransaction(context -> {
	    	StudySessionRepositoryInterface sessionRepo = context.getSessionRepository();
	    	TopicRepositoryInterface topicRepo = context.getTopicRepository();
	        Topic newTopic = new Topic(name, description, difficulty, new ArrayList<>());
	        for (Long sessionId : sessionIds) {
	            StudySession managedSession = sessionRepo.findById(sessionId);
	            if (managedSession == null) {
	                throw new IllegalArgumentException("Sessione con ID " + sessionId + " non trovata.");
	            }
	            newTopic.addSession(managedSession);
	        }
	        if(topicRepo.findByNameDescriptionAndDifficulty(name, description, difficulty) != null)
	            throw new IllegalArgumentException("Esiste già un topic con questi valori");
	        
	        topicRepo.save(newTopic);
	        return newTopic;
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
	public List<Topic> getAllTopics() {
		return tm.doInTopicTransaction(repository -> {
			try {
				return repository.findAll();
			} catch (Exception e) {
				throw new IllegalArgumentException("Errore durante il caricamento dei topic");
			}
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
			if(session.getTopicList().size() == 1) throw new IllegalArgumentException("la session deve avere almeno un topic");
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
