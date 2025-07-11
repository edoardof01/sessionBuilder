package com.sessionbuilder.core.backend;

import java.util.List;

public interface TopicRepositoryInterface {
	
	Topic findById(long id);
	void save(Topic topic);
	void update(Topic topic);
	void delete(long id);
	Topic findByNameDescriptionAndDifficulty(String name, String description, int difficulty);
	List<Topic> findAll();
}
