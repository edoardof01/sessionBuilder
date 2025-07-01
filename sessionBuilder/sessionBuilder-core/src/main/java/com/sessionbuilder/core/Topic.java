package com.sessionbuilder.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;

@Entity
public class Topic {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String name;
	private String description;
	private int difficulty;

	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(
			name = "Topic_StudySession",
			joinColumns = @JoinColumn(name = "topic_id"),
			inverseJoinColumns = @JoinColumn(name = "session_id")
	)
	private List<StudySession> sessionList = new ArrayList<>();
	
	private int masteryLevel = 0;

	public Topic(String name, String description, int difficulty, List<StudySession> sessionList) {
		if(name == null) throw new IllegalArgumentException("il nome non può essere null");
		this.name = name;
		this.description = description;
		if(difficulty <= 0 || difficulty > 5) throw new IllegalArgumentException("la difficulty deve essere positiva e minore di 5");
		this.difficulty = difficulty;
		this.sessionList = sessionList;
	}
	
	public Topic() {}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id; 
	}
	public String getName() {
		return this.name;
	}
	public String getDescription() {
		return this.description;
	}
	public int getDifficulty() {
		return this.difficulty;
	}
	public List<StudySession> getSessionList(){
		return this.sessionList;
	}
	public int getMasteryLevel() {
		return this.masteryLevel;
	}
	
	
	
	public void setSessions(List<StudySession> arrayList) {
		this.sessionList = arrayList;
	}
	void setMasteryLevel(int level) {
		this.masteryLevel = level;
	}
	
	public void addSession(StudySession session) {
		if(session == null) throw new IllegalArgumentException("la sessione non può essere nulla");
		this.sessionList.add(session);
		if(!session.getTopicList().contains(this)) {
			session.getTopicList().add(this);
		}
	}
	
	public void removeSession(StudySession session) {
		if(session == null) throw new IllegalArgumentException("la sessione da rimuovere è null");
		if(!this.sessionList.contains(session)) throw new IllegalArgumentException("sessione da rimuovere non trovata");
		this.sessionList.remove(session);
		if (session.getTopicList().contains(this)) {
			session.getTopicList().remove(this);
		}
	}
	
	public int totalTime() {
		return this.getSessionList().stream().
			map(StudySession::getDuration).
			reduce(0,((a,b)-> a+b));
	}

	
	public int percentageOfCompletion() {
		if(this.getSessionList().isEmpty()) return 0;
		return Math.round((float)(this.getSessionList().stream().filter(s -> s.isComplete()).
						count())/(this.getSessionList().size())*100);
	}

	void increaseMasteryLevel(int points) {
		this.masteryLevel += points;
	}


	void decreaseMasteryLevel(int points) {
		if(points < 0) throw new IllegalArgumentException("il valore dei punti da rimuovere deve essere positivo");
		if(masteryLevel < points) masteryLevel = 0; //NOPIT
		else this.masteryLevel -= points;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null || getClass() != obj.getClass()) return false;
		Topic other = (Topic) obj;
		return this.id > 0 && Objects.equals(this.id, other.id) ;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		int sessionCount = sessionList != null ? sessionList.size() : 0;
		return "Topic( name: "+ name + ", description: "+ description + ", difficulty: " + difficulty + ", numSessions: " + sessionCount +")";
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;	
	}

}
