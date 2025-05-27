package com.sessionBuilder.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;

public class Topic {
	
	private String name;
	private String description;
	private int difficulty;
	private List<StudySession> sessionList = new ArrayList<StudySession>();
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
	
	void setSessions(ArrayList<StudySession> arrayList) {
		this.sessionList = arrayList;
	}

	
	void setMasteryLevel(int level) {
		this.masteryLevel = level;
	}
	
	public void addSession(StudySession session) {
		if(session == null) throw new IllegalArgumentException("la sessione non può essere nulla");
		this.sessionList.add(session);
	}
	
	public void removeSession(StudySession session) {
		if(session == null) throw new IllegalArgumentException("la sessione da rimuovere è null");
		if(!this.sessionList.contains(session)) throw new IllegalArgumentException("sessione da rimuovere non trovata");
		this.sessionList.remove(session);
	}
	
	public int totalTime() {
		return this.getSessionList().stream().
			map(StudySession::getDuration).
			reduce(0,((a,b)-> a+b));
	}

	public int percentageOfCompletion() {
		if(this.getSessionList().size() == 0) return 0;
		return Math.round((float)(this.getSessionList().stream().filter(s -> s.isComplete()).
						count())/(this.getSessionList().size())*100);
	}

	public void increaseMasteryLevel(int points) {
		this.masteryLevel += points;
	}

	
	public void decreaseMasteryLevel(int points) {
		if(points < 0) throw new IllegalArgumentException("il valore dei punti da rimuovere deve essere positivo");
		if(masteryLevel < points) masteryLevel = 0; //NOPIT
		else this.masteryLevel -= points;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null || getClass() != obj.getClass()) return false;
		Topic other = (Topic) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.description, other.description)
				&& this.difficulty == other.getDifficulty()
				&& Objects.equals(this.sessionList, other.sessionList);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, description, difficulty, sessionList);
	}

	

	
	



}
