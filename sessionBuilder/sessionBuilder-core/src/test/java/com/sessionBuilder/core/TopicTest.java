package com.sessionBuilder.core;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.Before;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TopicTest {
	
	private StudySession session1;
	private String name1;
	private String description1;
	private int difficulty1;
	private List<StudySession> sessionList;
	private StudySession fullSession1;
	private StudySession fullSession2;
	private Topic topic2;
	private Topic topic3;
	// FIELS FOR EQUALS
	private ArrayList<StudySession> emptySessions;
	private Topic topicA;
	private Topic topicB;

	
	@Before
	public void setup() {
		session1 = new StudySession();
		name1 = "Programming";
		description1 = "Una descrizione del topic";
		difficulty1 = 2;
		sessionList = new ArrayList<StudySession>(List.of(session1));
		topic2 = new Topic("giardinaggio","alberi da frutto e piante da fiore", 2, 
				new ArrayList<StudySession>());
		fullSession1 = new StudySession(LocalDate.now().plusDays(1), 60, "impara le basi leggendo libri", 
				new ArrayList<Topic>(List.of(topic2)));
		fullSession2 = new StudySession(LocalDate.now().plusDays(3), 120, "acquista terriccio, vasi, semi, ...",
				new ArrayList<Topic>(List.of(topic2)));
		topic2.setSessions(new ArrayList<>(List.of(fullSession1,fullSession2)));
		topic3 = new Topic("musica","impara a suonare il pianoforte", 4, new ArrayList<StudySession>());
		
		// FIELDS FOR TESTING EQUALS
		emptySessions = new ArrayList<>();
		topicA = new Topic("matematica", "Studia matematica", 3, emptySessions);
		topicB = new Topic("matematica", "Studia matematica", 3, emptySessions);
	}
	
	@Test
	public void testCreatingTopicWithNullNameFieldFailure() {
		assertThrows(IllegalArgumentException.class, () ->
			new Topic(null, description1, difficulty1, sessionList)
		);
	}

	@Test
	public void testCreatingTopicWithDifficultyFieldZeroFailure() {
		assertThrows(IllegalArgumentException.class, () ->
			new Topic(name1, description1, 0, sessionList)
		);
	}

	@Test
	public void testCreatingTopicWithNegativeDifficultyFieldFailure() {
		assertThrows(IllegalArgumentException.class, () ->
			new Topic(name1, description1, -1, sessionList)
		);
	}

	@Test
	public void testCreatingTopicWithDifficultyFieldExceedingMaxFailure() {
		assertThrows(IllegalArgumentException.class, () ->
			new Topic(name1, description1, 6, sessionList)
		);
	}
	
	@Test
	public void testCreatingTopicWithValidFieldsSuccess() {
		Topic topic = new Topic(name1, description1, difficulty1, sessionList);
		assertThat(topic.getName()).isEqualTo(name1);
		assertThat(topic.getDescription()).isEqualTo(description1);
		assertThat(topic.getDifficulty()).isEqualTo(difficulty1);
	}
	
	@Test
	public void testAddSesssionToTopicSuccess() {
		Topic topic = new Topic("arte", "rinascimento", 3, new ArrayList<>());
		StudySession session = new StudySession(LocalDate.now(), 60, "una nota", new ArrayList<>(List.of(topic)));
		topic.addSession(session);
		assertThat(topic.getSessionList()).contains(session);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddNullSessionToTopicFailure() {
		Topic topic = new Topic("arte","rinascimento",3, new ArrayList<>());
		topic.addSession(null);
	}
	
	@Test
	public void testRemoveSessionFromTopicSuccess() {
		StudySession session = new StudySession(LocalDate.now(), 60, "una nota", new ArrayList<>());
		Topic topic = new Topic("arte", "rinascimento", 3, new ArrayList<>());
		topic.setId(1L);
		topic.setSessions(new ArrayList<>(List.of(session)));
		topic.removeSession(session);
		assertThat(topic.getSessionList()).doesNotContain(session);
		assertThat(topic.getId()).isEqualTo(1L);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveNullSessionFromTopicFailure() {
		Topic topic = new Topic("arte", "rinascimento", 3, new ArrayList<>());
		topic.removeSession(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveNonExistentSessionFromTopicFailure() {
		Topic topic = new Topic("arte", "rinascimento", 3, new ArrayList<>());
		StudySession existingSession = new StudySession(LocalDate.now(), 60, "una nota", new ArrayList<>(List.of(topic)));
		StudySession nonExistingSession = new StudySession(LocalDate.now().plusDays(1), 90, "altra nota", new ArrayList<>(List.of(topic)));
		topic.setSessions(new ArrayList<>(List.of(existingSession)));
		topic.removeSession(nonExistingSession);
	}
	
	@Test
	public void testCalculateTotalTimePerTopicSuccess() {
		assertThat(topic2.totalTime()).isEqualTo(180);
	}
	
	@Test
	public void testCalculatePercentageOfTopicCompletionWithoutSessions() {
		assertThat(topic3.percentageOfCompletion()).isZero();
	}
	
	
	@Test
	public void testCalculatePercentageOfTopicCompletion() {
		StudySession session = new StudySession(LocalDate.now().plusDays(3), 120, "impara a suonare fra martino",
				new ArrayList<Topic>(List.of(topic3)));
		session.setIsComplete(true);
		topic3.setSessions(new ArrayList<StudySession>(List.of(session,fullSession1)));
		assertThat(topic3.percentageOfCompletion()).isEqualTo(50);
	}
	
	@Test
	public void testIncreaseMasteryLevelSuccess() {
		topic2.setMasteryLevel(0);
		topic2.increaseMasteryLevel(10);
		assertThat(topic2.getMasteryLevel()).isEqualTo(10);
	}
	
	@Test
	public void testBringMasteryLevelToNegativeValueFailure() {
		topic2.setMasteryLevel(5);
		topic2.decreaseMasteryLevel(10);
		assertThat(topic2.getMasteryLevel()).isZero();
	}
	
	@Test
	public void testDecreaseMastreryLevelWithAnInvalidValueFailure() {
		topic2.setMasteryLevel(10);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				()-> topic2.decreaseMasteryLevel(-10));
		assertThat(e.getMessage()).isEqualTo("il valore dei punti da rimuovere deve essere positivo");
	}
	
	
	
	// TEST PER EQUALS, HASHCODE & TOSTRING
	@Test
	public void testEquals() {
		assertThat(topicA.equals(topicA)).isTrue();
	}
	
	@Test
	public void testEqualsTopics() {
		assertThat(topicA.equals(topicB)).isTrue();
	}
	
	@Test
	public void testNotEqualsNull() {
		assertThat(topicA).isNotEqualTo(null);
	}
	
	@Test 
	public void testNotEqualsDifferentType() {
		assertThat(topicA).isNotEqualTo("non un topic");
	}
	
	@Test
	public void testNotEqualsDifferentName() {
		Topic other = new Topic("fisica", "Studia matematica", 3, emptySessions);
		assertThat(topicA).isNotEqualTo(other);
	}
	
	@Test
	public void testNotEqualsDifferentDescription() {
		Topic other = new Topic("matematica", "Different description", 3, emptySessions);
		assertFalse(topicA.equals(other));
	}
	
	@Test
	public void testNotEqualsDifferentDifficulty() {
		Topic other = new Topic("matematica", "Studia matematica", 4, emptySessions);
		assertFalse(topicA.equals(other));
	}
	
	@Test
	public void testEqualsDifferentSessionList() {
		List<StudySession> sessions1 = new ArrayList<>();
		List<StudySession> sessions2 = new ArrayList<>();
		StudySession s = new StudySession(LocalDate.now(), 30, "Note", new ArrayList<>());
		sessions2.add(s);
		Topic t1 = new Topic("matematica", "Studia matematica", 3, sessions1);
		Topic t2 = new Topic("matematica", "Studia matematica", 3, sessions2);
		assertThat(t1).isEqualTo(t2);
	}
	
	@Test
	public void testHashCodeEqualObjects() {
		assertThat(topicA).hasSameHashCodeAs(topicB);
	}
	
	@Test
	public void testHashCodeConsistency() {
		int hc = topicA.hashCode();
		assertThat(hc).isEqualTo(topicA.hashCode()).isEqualTo(topicA.hashCode());
	}
	
	@Test
	public void testHashCodeDifferentObjects() {
		Topic other = new Topic("Cinema", "guarda film di Tarantino", 2, new ArrayList<>());
		assertThat(topicA.equals(other)).isFalse();
	}
	
	@Test
	public void testHashCodeNotZeroForValidTopic() {
		assertThat(topicA.hashCode()).isNotZero();
	}
	
	@Test
	public void testToStringWithNonNullSessionList() {
		Topic topic = new Topic("Java", "Programming language", 3, new ArrayList<>());
		String expected = "Topic( name: Java, description: Programming language, difficulty: 3, numSessions: 0)";
		assertThat(topic).hasToString(expected);
	}

	@Test
	public void testToStringWithNullSessionList() {
		Topic topic = new Topic();
		topic.setSessions(null);
		String result = topic.toString();
		assertThat(result).contains("numSessions: 0");
	}

	@Test
	public void testToStringWithMultipleSessions() {
	   ArrayList<StudySession> sessions = new ArrayList<>();
	   LocalDate date = LocalDate.now().plusDays(2);
	   ArrayList<Topic> topics = new ArrayList<>();
	   
	   StudySession session3 = new StudySession(date, 60, "Session 1", topics);
	   StudySession session2 = new StudySession(date.plusDays(1), 90, "Session 2", topics);
	   
	   sessions.add(session3);
	   sessions.add(session2);
	   
	   Topic topic = new Topic("Chimica", "reazioni chimiche", 4, sessions);
	   String result = topic.toString();
	   
	   assertTrue(result.contains("Chimica"));
	   assertTrue(result.contains("reazioni chimiche"));
	   assertTrue(result.contains("4"));
	   assertTrue(result.contains("2"));
	}
	
	@Test
	public void testSetNameAndDescription() {
		Topic topic = new Topic("Original Name", "Description", 3, new ArrayList<>());
		assertThat(topic.getName()).isEqualTo("Original Name");
		topic.setName("New Name");
		assertThat(topic.getName()).isEqualTo("New Name");
		topic.setDescription("New Description");
		assertThat(topic.getDescription()).isEqualTo("New Description");
	}
	
	
	
	
	
	
	
	

}
