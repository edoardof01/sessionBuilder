package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
public class StudySession {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private LocalDate date;
	private int duration;
	private String note;
	
	@ManyToMany(mappedBy="sessionList")
	private List<Topic> topicList = new ArrayList<Topic>();
	
	private boolean isComplete;
	

	public StudySession(LocalDate date, int duration, String note, ArrayList<Topic> topicList) {
		if(date == null) throw new IllegalArgumentException("la date non può essere null");
		if(date.isBefore(LocalDate.now())) throw new IllegalArgumentException("la date non può essere nel passato");
		this.date = date;
		if(duration<=0) throw new IllegalArgumentException("la durata deve essere positiva");
		this.duration = duration;
		if(note == null) throw new IllegalArgumentException("la note non può essere null");
		this.note = note;
		if(topicList == null) throw new IllegalArgumentException("deve esserci almeno un topic");
		if(topicList.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("almeno un Topic è null");
		this.topicList = topicList;
		this.isComplete = false;
	}
	
	public StudySession() {}
	public LocalDate getDate() {
		return this.date;
	}
	public Integer getDuration() {
		return this.duration;
	}
	public String getNote() {
		return this.note;
	}
	public List<Topic> getTopicList() {
		return this.topicList;
	}
	public boolean isComplete() {
		return this.isComplete;
	}
	void setId(long id) {
		this.id = id;
	}
	
	void setIsComplete(boolean value) {
		this.isComplete = value;
	}

	public void addTopic(Topic topic) {
		if(topic == null) throw new IllegalArgumentException("null Topic");
		if(this.isComplete()) throw new IllegalStateException("non si possono aggiungere topic alle sessioni completate");
		this.getTopicList().add(topic);
	}

	public void removeTopic(Topic topic) {
		if(topic == null) throw new IllegalArgumentException("null Topic");
		if(this.getTopicList().size() == 1) throw new IllegalArgumentException("Una sessione deve avere almeno un Topic");
		if(this.isComplete()) throw new IllegalStateException("non si possono rimuovere topic dalle sessioni completate");
		boolean found = topicList.contains(topic);
		if (!found) throw new IllegalArgumentException("il topic non è presente nella lista");
		this.getTopicList().remove(topic);
	}

	public void complete() {
		if(this.isComplete) throw new IllegalArgumentException("la sessione è già stata completata");
		for (Topic topic : topicList) {
			int basePoints = 10;
			if(this.duration >= 90) basePoints += 5;
			if(topic.getDifficulty() == 5) basePoints += 5;
			if(topic.getDifficulty() >= 3 && this.duration < 90) basePoints -= 2;
			topic.increaseMasteryLevel(basePoints);
		}
		this.setIsComplete(true);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null || getClass() != obj.getClass()) return false;
		StudySession other = (StudySession) obj;
		return Objects.equals(this.date, other.getDate()) && this.duration == other.getDuration() &&
				Objects.equals(this.note, other.note) && Objects.equals(this.topicList, other.topicList);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(date, duration, note, topicList);
	}

	




}
