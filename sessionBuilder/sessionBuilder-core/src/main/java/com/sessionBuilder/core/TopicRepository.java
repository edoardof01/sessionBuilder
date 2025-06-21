package com.sessionBuilder.core;

import com.google.inject.Inject;

import jakarta.persistence.NoResultException;


public class TopicRepository implements TopicRepositoryInterface{
	
	
	private TransactionManager tm;
	
	@Inject
	public TopicRepository(TransactionManager tm) {
		this.tm = tm;
	}

	@Override
	public Topic findById(long id) {
		return tm.doInTransaction(em -> {
			try {
				return em.createQuery(
					"SELECT t FROM Topic t LEFT JOIN FETCH t.sessionList WHERE t.id = :id", 
					Topic.class)
					.setParameter("id", id)
					.getSingleResult();
			} catch (NoResultException e) {
				throw new IllegalArgumentException("non esiste un topic con tale id");
			}
		});
	}

	@Override
	public void save(Topic topic) {
		tm.doInTransaction(em -> {
			if(topic == null) throw new IllegalArgumentException("il topic da persistere è null");
			em.persist(topic);
			return null;
		});
	}

	@Override
	public void update(Topic topic) {
		tm.doInTransaction(em -> {
			if(topic == null) throw new IllegalArgumentException("il topic da aggiornare è null");
			em.merge(topic);
			return null;
		});
	}
	
	@Override
	public void delete(long id) {
		tm.doInTransaction(em -> {
			Topic result = em.find(Topic.class, id);
			if(result == null) throw new IllegalArgumentException("il topic da rimuovere è null");
			em.remove(result);
			return null;
		});
	}

	@Override
	public Topic findByNameDescriptionAndDifficulty(String name, String description, int difficulty) {
		return tm.doInTransaction(em ->{
			try {
			return em.createQuery(
				"SELECT t FROM Topic t WHERE t.name = :name AND t.description = :description AND t.difficulty = :difficulty",
				Topic.class)
				.setParameter("name", name)
				.setParameter("description", description)
				.setParameter("difficulty", difficulty)
				.getSingleResult();
			} catch (Exception e) {
				return null;
			}
		});
		
	}

	

	


}
