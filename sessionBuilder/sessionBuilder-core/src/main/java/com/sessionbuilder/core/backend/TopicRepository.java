package com.sessionbuilder.core.backend;

import java.util.List;
import com.google.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class TopicRepository implements TopicRepositoryInterface{

	private TransactionManager tm;
	
	@Inject
	public TopicRepository(TransactionManager tm) {
		this.tm = tm;
	}

	@Override
	public Topic findById(long id) {
		EntityManager em = tm.getCurrentEntityManager();
		try {
			return em.createQuery(
				"SELECT t FROM Topic t LEFT JOIN FETCH t.sessionList WHERE t.id = :id", 
				Topic.class)
				.setParameter("id", id)
				.getSingleResult();
		} catch (NoResultException e) {
			throw new IllegalArgumentException("non esiste un topic con tale id");
		}
	}
	
	@Override
	public Topic findByNameDescriptionAndDifficulty(String name, String description, int difficulty) {
		EntityManager em = tm.getCurrentEntityManager();
		List<Topic> topics = em.createQuery(
			"SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty",
			Topic.class)
			.setParameter("name", name)
			.setParameter("description", description)
			.setParameter("difficulty", difficulty)
			.getResultList();
		
		if (topics.isEmpty()) {
			return null;
		} else {
			return topics.get(0);
		}
	}
	
	@Override
	public List<Topic> findAll() {
		EntityManager em = tm.getCurrentEntityManager();
		try {
			return em.createQuery("SELECT t FROM Topic t", Topic.class).getResultList();
		} catch(Exception e) {
			throw new IllegalArgumentException("erorre nell'estrazione dei topic");
		}
	}

	@Override
	public void save(Topic topic) {
		EntityManager em = tm.getCurrentEntityManager();
		if(topic == null) {
			throw new IllegalArgumentException("il topic da persistere è null");
		}
		em.persist(topic);
	}

	@Override
	public void update(Topic topic) {
		EntityManager em = tm.getCurrentEntityManager();
		if(topic == null) {
			throw new IllegalArgumentException("il topic da aggiornare è null");
		}
		em.merge(topic);
	}
	
	@Override
	public void delete(long id) {
		EntityManager em = tm.getCurrentEntityManager();
		Topic result = em.find(Topic.class, id);
		if(result == null) {
			throw new IllegalArgumentException("il topic da rimuovere è null");
		}
		em.remove(result);
	}
}