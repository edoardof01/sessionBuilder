package com.sessionbuilder.core.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
	
	@ManyToMany(mappedBy="sessionList",fetch = FetchType.EAGER)
	private List<Topic> topicList = new ArrayList<>();

	private boolean isComplete;

	public StudySession(LocalDate date, int duration, String note, List<Topic> topicList) {
		if(date == null) throw new IllegalArgumentException("la date non può essere null");
		if(date.isBefore(LocalDate.now())) throw new IllegalArgumentException("la date non può essere nel passato");
		this.date = date;
		if(duration <= 0) throw new IllegalArgumentException("la durata deve essere positiva");
		this.duration = duration;
		if(note == null) throw new IllegalArgumentException("la note non può essere null");
		this.note = note;
		if(topicList == null) throw new IllegalArgumentException("deve esserci almeno un topic");
		if(topicList.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("almeno un Topic è null");
		for(Topic topic : topicList) {
			topic.addSession(this);
		}
		this.isComplete = false;
		this.topicList = topicList;
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
	
	//NOSONAR
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public void setIsComplete(boolean value) {
		this.isComplete = value;
	}

	public void addTopic(Topic topic) {
		if(topic == null) throw new IllegalArgumentException("null Topic");
		if(this.isComplete()) throw new IllegalStateException("non si possono aggiungere topic alle sessioni completate");
		this.getTopicList().add(topic);
		if(!topic.getSessionList().contains(this)) {
			topic.addSession(this);
		}
	}

	public void removeTopic(Topic topic) {
		if(topic == null) throw new IllegalArgumentException("null Topic");
		if(this.getTopicList().size() == 1) throw new IllegalArgumentException("Una sessione deve avere almeno un Topic");
		if(this.isComplete()) throw new IllegalStateException("non si possono rimuovere topic dalle sessioni completate");
		boolean found = topicList.contains(topic);
		if (!found) throw new IllegalArgumentException("il topic non è presente nella lista");
		this.getTopicList().remove(topic);
		if (topic.getSessionList().contains(this)) {
			topic.removeSession(this);
		}
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
		return this.id > 0 && Objects.equals(this.id, other.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void setTopics (List<Topic> topics) {
		this.topicList = topics;
	}
	
	@Override
	public String toString() {
		String topicNames = topicList.stream().map(Topic::getName).collect(Collectors.joining(", "));
		String completedStatus = isComplete ? "Completed: true" : "Completed: false";
		return "StudySession("+ date + ", "+ duration + ", " + note + ", " + completedStatus + ", topics{" + topicNames + "})";
	}


	




}
