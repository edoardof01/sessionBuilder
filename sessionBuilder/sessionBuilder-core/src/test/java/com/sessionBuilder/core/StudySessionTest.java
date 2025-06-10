package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;


import org.junit.Before;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudySessionTest {
	
	private Topic topic1;
	private String note;
	private LocalDate date;
	private ArrayList<Topic> topics;
	private Topic fullTopic;
	private StudySession session;
	
	//campi per l'equals
	private StudySession s1;
	private StudySession s2;


	@Before
	public void setup() {
		topic1 = new Topic("Mercato immobiliare", "case a Firenze", 1, new ArrayList<>());
		note = "this is a note about the session";
		date = LocalDate.now().plusDays(1);
		topics = new ArrayList<Topic>(List.of(topic1));
		fullTopic = new Topic("scacchi","impara nuove aperture", 5, new ArrayList<StudySession>());
		session = new StudySession(date, 60, note, topics);
		//Campi per l'equals 
		s1 = new StudySession(date, 60, note, topics);
		s2 = new StudySession(date, 60, note, topics);
	}

	@Test
	public void testSessionCreationSuccess() {
		StudySession studySession = new StudySession(date ,60, note, topics);
		studySession.setId(1L);
		assertThat(studySession.getDate()).isEqualTo(date);
		assertThat(studySession.getDuration()).isEqualTo(60);
		assertThat(studySession.getNote()).isEqualTo(note);
		assertThat(studySession.getTopicList()).containsAll(topics);
		assertThat(studySession.isComplete()).isFalse();
		assertThat(studySession.getId()).isEqualTo(1);
	
	}
	
	@Test
	public void testSessionCreationWIthDateNowSuccess() {
		StudySession studySession = new StudySession(LocalDate.now() ,60, note, topics);
		assertThat(studySession.getDate()).isEqualTo(LocalDate.now());
		assertThat(studySession.getDuration()).isEqualTo(60);
		assertThat(studySession.getNote()).isEqualTo(note);
		assertThat(studySession.getTopicList()).containsAll(topics);
		assertThat(studySession.isComplete()).isFalse();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationWithNullDateFailure() {
		StudySession studySession = new StudySession(null , 60 ,note, topics);	
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationWithPastDateFailure() {
		StudySession studySession = new StudySession(LocalDate.now().minusDays(1) , 60 ,note, topics);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationInstantaneuosDurationFailure() {
		StudySession studySession = new StudySession(date , 0 ,note, topics);
	}
	
	@Test( expected = IllegalArgumentException.class)
	public void testSessionCreationWithNullNoteFailure() {
		StudySession studySession = new StudySession(date , 60 , null, topics);
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationNegativeDurationFailure() {
		StudySession studySession = new StudySession(date , -1 ,note, topics);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationWithNullTopicListFailure() {
		StudySession studySession = new StudySession(date ,60 ,note , null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationWithOneNullTopicFailure() {
		ArrayList<Topic> nullTopics = new ArrayList<Topic>();
		nullTopics.add(null);
		StudySession studySession = new StudySession(date ,60 , note, nullTopics);	
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void testSessionCreationWithANullTopicTooFailure() {
		ArrayList<Topic> topics = new ArrayList<Topic>();
		topics.add(fullTopic);
		topics.add(null);
		StudySession studySession = new StudySession(date ,60 , note, topics);	
	}
	
	
	@Test
	public void testAddingNullTopicToUnCompletedSessionFailure() {
		Topic topic2 = null;
		StudySession studySession = new StudySession(date ,60, note, topics);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()-> studySession.addTopic(topic2));
		assertThat(e.getMessage()).isEqualTo("null Topic");
	}
	
	@Test
	public void testAddingTopicThatAlreadyContainsSession() {
	   Topic newTopic = new Topic("Nuovo Topic", "Descrizione", 3, new ArrayList<>());
	   newTopic.getSessionList().add(session);
	   assertThat(session.getTopicList()).containsExactly(topic1);
	   assertThat(newTopic.getSessionList()).containsExactly(session);
	   session.addTopic(newTopic);   
	   assertThat(session.getTopicList()).containsExactly(topic1, newTopic);
	   assertThat(newTopic.getSessionList()).containsExactly(session);
	}
	
	@Test
	public void testAddingTopicToCompletedSessionFailure() {
		Topic topic2 = new Topic();
		StudySession studySession = new StudySession(date ,60, note, topics);
		studySession.setIsComplete(true);
		IllegalStateException e = assertThrows(IllegalStateException.class,
				()-> studySession.addTopic(topic2));
		assertThat(e.getMessage()).isEqualTo("non si possono aggiungere topic alle sessioni completate");
	}
	
	@Test
	public void testAddingTopicThatDoesNotContainSession() {
		Topic newTopic = new Topic("Nuovo Topic", "Descrizione", 3, new ArrayList<>());
		
		assertThat(newTopic.getSessionList()).doesNotContain(session);
		assertThat(session.getTopicList()).containsExactly(topic1);
		session.addTopic(newTopic);
		assertThat(session.getTopicList()).containsExactly(topic1, newTopic);
		assertThat(newTopic.getSessionList()).contains(session);
	}
	
	@Test
	public void testAddingTopicAlreadyOwningTheSessionSuccess(){
		fullTopic.setSessions(new ArrayList<>(List.of(session)));
		session.addTopic(fullTopic);
		assertThat(session.getTopicList()).containsExactly(topic1, fullTopic);
	}

	@Test
	public void testAddingTopicsToUnCompletedSessionSuccess() {
		Topic topic2 = new Topic();
		ArrayList<Topic> topics = new ArrayList<Topic>();
		topics.add(topic1);
		
		StudySession studySession = new StudySession(date ,60, note, topics);
		studySession.addTopic(topic2);
		assertThat(studySession.getTopicList()).containsAll(new ArrayList<Topic>(List.of(topic1,topic2)));
	}
	
	@Test
	public void testRemovingLastTopicInTheListFailure() {
		StudySession studySession = new StudySession(date ,60, note, topics);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()-> studySession.removeTopic(topic1));
		assertThat(e.getMessage()).isEqualTo("Una sessione deve avere almeno un Topic");
	}
	
	@Test
	public void testRemoveTopicAlsoRemovesSessionFromTopic() {
		Topic topic2 = new Topic("Secondo Topic", "Descrizione", 3, new ArrayList<>());
		topics.add(topic2);
		StudySession studySession = new StudySession(date, 60, note, topics);
		assertThat(studySession.getTopicList()).contains(topic2);
		assertThat(topic2.getSessionList()).contains(studySession);
		studySession.removeTopic(topic2);
		assertThat(studySession.getTopicList()).doesNotContain(topic2);
		assertThat(topic2.getSessionList()).doesNotContain(studySession);
	}
	
	@Test
	public void testRemovingNullTopicToUnCompletedSessionFailure() {
		Topic topic2 = null;
		StudySession studySession = new StudySession(date ,60, note, topics);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()-> studySession.removeTopic(topic2));
		assertThat(e.getMessage()).isEqualTo("null Topic");
	}
	

	

	@Test 
	public void testRemovingTopicsToCompletedSessionFailure() {
		Topic topic2 = new Topic();
		topics.add(topic2);
		StudySession studySession = new StudySession(date ,60, note, topics);
		studySession.setIsComplete(true);
		IllegalStateException e = assertThrows(IllegalStateException.class,
				()-> studySession.removeTopic(topic2));
		assertThat(e.getMessage()).isEqualTo("non si possono rimuovere topic dalle sessioni completate");
	}
	
	@Test
	public void testRemovingAbsentTopicFailure() {
		Topic topic = new Topic("Cultura Calcistica","impara le formazioni delle squadre più importanti",1,
				new ArrayList<StudySession>());
		Topic topic3 = new Topic("Cucina","impara primi piatti",2,
				new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>();
		topics.add(topic);
		topics.add(topic3);
		StudySession studySession = new StudySession(date , 60, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic3.setSessions(new ArrayList<>(List.of(studySession)));
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()-> studySession.removeTopic(fullTopic));
		assertThat(e.getMessage()).isEqualTo("il topic non è presente nella lista");
		assertThat(studySession.getTopicList()).containsAll(topics);
	}
	
	@Test 
	public void testRemovingTopicSuccess() {
		Topic topic = new Topic("Cultura Calcistica","impara le formazioni delle squadre più importanti",1,
				new ArrayList<StudySession>());
		Topic topic3 = new Topic("Cucina","impara primi piatti",2,
				new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>();
		topics.add(topic);
		topics.add(topic3);
		StudySession studySession = new StudySession(date , 60, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic3.setSessions(new ArrayList<>(List.of(studySession)));
		studySession.removeTopic(topic3);
		assertThat(studySession.getTopicList()).doesNotContain(topic3).containsExactly(topic);
	}
	
	@Test
	public void testCompleteSessionMaxPointsSuccess() {
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(fullTopic));
		StudySession studySession = new StudySession(date , 120, note, topics);
		fullTopic.setSessions(new ArrayList<>(List.of(studySession)));
		fullTopic.setMasteryLevel(0);
		studySession.complete();
		assertThat(fullTopic.getMasteryLevel()).isEqualTo(20);
	}
	
	@Test
	public void testCompleteSessionWithoutMaxDifficultyAndLongDurationSuccess() {
		Topic topic = new Topic("biliardino","impara a giocare in difesa", 4, new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(topic));
		StudySession studySession = new StudySession(date , 120, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic.setMasteryLevel(0);
		studySession.complete();
		assertThat(topic.getMasteryLevel()).isEqualTo(15);
	}
	
	@Test 
	public void testCompleteSessionWithLittleDifficultyAndDUrationSuccess() {
		Topic topic = new Topic("viaggi","visita paesi europei", 2, new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(topic));
		StudySession studySession = new StudySession(date , 60, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic.setMasteryLevel(0);
		studySession.complete();
		assertThat(topic.getMasteryLevel()).isEqualTo(10);
	};
	
	@Test
	public void testCompleteSessionExactlyNinetyMinutesBoundary() {
		Topic topic = new Topic("pesca", "pesca da traino", 2, new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(topic));
		StudySession studySession = new StudySession(date, 90, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic.setMasteryLevel(0);
		studySession.complete();
		assertThat(topic.getMasteryLevel()).isEqualTo(15);
	}
	
	@Test
	public void testCompleteSessionWithLowDifficultyAndHighDuration() {
		Topic topic = new Topic("Cultura Generale","informati sulle notizie del mondo", 2, new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(topic));
		StudySession studySession = new StudySession(date , 100, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic.setMasteryLevel(0);
		studySession.complete();
		assertThat(topic.getMasteryLevel()).isEqualTo(15);
	}
	
	@Test
	public void testCompleteSEssionDifficultyThreeWithEightyNineMinutes() {
		Topic topic = new Topic("Storia","Roma Monarchica", 3, new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(topic));
		StudySession studySession = new StudySession(date , 89, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic.setMasteryLevel(0);
		studySession.complete();
		assertThat(topic.getMasteryLevel()).isEqualTo(8);
	}
	
	@Test 
	public void testCompleteSessionDifficultyThreeWithNinetyMinutes() {
		Topic topic = new Topic("Excel","tabelle pivot", 3, new ArrayList<StudySession>());
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(topic));
		StudySession studySession = new StudySession(date , 90, note, topics);
		topic.setSessions(new ArrayList<>(List.of(studySession)));
		topic.setMasteryLevel(0);
		studySession.complete();
		assertThat(topic.getMasteryLevel()).isEqualTo(15);
	}
	
	@Test
	public void testCompleteSessionWithPenaltySuccess() {
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(fullTopic));
		StudySession studySession = new StudySession(date , 50, note, topics);
		fullTopic.setSessions(new ArrayList<>(List.of(studySession)));
		fullTopic.setMasteryLevel(0);
		studySession.complete();
		assertThat(fullTopic.getMasteryLevel()).isEqualTo(13);
	}
	
	@Test
	public void testCompleteSessionAlreadyCompleted() {
		ArrayList<Topic> topics = new ArrayList<Topic>(List.of(fullTopic));
		StudySession studySession = new StudySession(date , 50, note, topics);
		fullTopic.setSessions(new ArrayList<>(List.of(studySession)));
		fullTopic.setMasteryLevel(0);
		studySession.complete();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()->studySession.complete());
		assertThat(e.getMessage()).isEqualTo("la sessione è già stata completata");
		assertThat(fullTopic.getMasteryLevel()).isEqualTo(13);
	}
	
	// TEST PER L'EQUALS & HASHCODE
	@Test
	public void testIsEqual() {
		assertThat(s1.equals(s1)).isTrue();
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualsDifferentClass() {
		assertThat(s1.equals("non una sessione")).isFalse();
	}
	
	@Test
	public void testEqualsNull() {
		assertThat(s1.equals(null)).isFalse();
	}
	
	@Test
	public void testEqualsDifferentDate() {
		StudySession other = new StudySession(date.plusDays(1), 60, note, topics);
		assertThat(s1.equals(other)).isFalse();
	}
	
	@Test
	public void testEqualsDifferentDuration() {
		StudySession other = new StudySession(date, 30, note, topics);
		assertThat(s1.equals(other)).isFalse();
	}
	
	@Test
	public void testEqualsIdenticalObjects() {
		StudySession session1 = new StudySession(date, 60, note, topics);
		StudySession session2 = new StudySession(date, 60, note, topics);
		assertThat(session1.equals(session2)).isTrue();
	}
	
	@Test
	public void testEqualsDifferentNote() {
		StudySession other = new StudySession(date, 30, "una nota diversa", topics);
		assertThat(s1.equals(other)).isFalse();
	}
	
	@Test
	public void testEqualsDifferentTopics() {
		StudySession other = new StudySession(date, 60, note, new ArrayList<>(List.of(fullTopic)));
		fullTopic.setSessions(new ArrayList<>(List.of(other)));
		assertThat(s1.equals(other)).isTrue();
	}
	
	@Test
	public void testEqualsDifferentNoteAndTopics() {
		StudySession other = new StudySession(date, 60, "nota diversa", new ArrayList<>(List.of(fullTopic)));
		fullTopic.setSessions(new ArrayList<>(List.of(other)));
		assertThat(s1.equals(other)).isFalse();
	}
	
	@Test
	public void testHashCodeEqualsObjects() {
		assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
	}
	
	@Test
	public void testHashCodeConsistency() {
		assertThat(s1.hashCode()).isEqualTo(s1.hashCode());
		assertThat(s1.hashCode()).isEqualTo(s1.hashCode());
	}
	
	@Test
	public void testHashCodeDifferentObjects() {
		StudySession other = new StudySession(date.plusDays(1), 60, note, topics);
		assertThat(s1.hashCode()).isNotEqualTo(other.hashCode());
	}
	
	@Test
	public void testToStringWithEmptyTopicList() {
		LocalDate date = LocalDate.of(2025, 6, 10);
		StudySession session = new StudySession(date, 60, "test note", new ArrayList<>());
		String expected = "StudySession(2025-06-10, 60, test note, topics{})";
		assertThat(session.toString()).isEqualTo(expected);
	}

	@Test
	public void testToStringWithSingleTopic() {
		LocalDate date = LocalDate.of(2025, 6, 10);
		ArrayList<Topic> topics = new ArrayList<>();
		Topic topic = new Topic("Java", "Programming", 3, new ArrayList<>());
		topics.add(topic);
		
		StudySession session = new StudySession(date, 90, "Study session", topics);
		String expected = "StudySession(2025-06-10, 90, Study session, topics{Java})";
		assertThat(session.toString()).isEqualTo(expected);
	}

	@Test
	public void testToStringWithMultipleTopics() {
		LocalDate date = LocalDate.of(2025, 6, 10);
		ArrayList<Topic> topics = new ArrayList<>();
		Topic topic1 = new Topic("Java", "Programming", 3, new ArrayList<>());
		Topic topic2 = new Topic("Matematica", "studia integrali", 4, new ArrayList<>());
		Topic topic3 = new Topic("Fisica", "meccanica", 5, new ArrayList<>());
		topics.add(topic1);
		topics.add(topic2);
		topics.add(topic3);
		
		StudySession session = new StudySession(date, 120, "più topics", topics);
		String expected = "StudySession(2025-06-10, 120, più topics, topics{Java, Matematica, Fisica})";
		assertThat(session.toString()).isEqualTo(expected);
	}
	
	@Test
	public void testSetDuration() {
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "Test note", new ArrayList<>());
		assertThat(session.getDuration()).isEqualTo(60);
		session.setDuration(90);
		assertThat(session.getDuration()).isEqualTo(90);
	}
	
	
	
	
		


}
