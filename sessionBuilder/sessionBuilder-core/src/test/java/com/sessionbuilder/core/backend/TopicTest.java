package com.sessionbuilder.core.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;



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

	@Before
	public void setup() {
		session1 = new StudySession();
		name1 = "Programming";
		description1 = "Una descrizione del topic";
		difficulty1 = 2;
		sessionList = new ArrayList<>(List.of(session1));
		topic2 = new Topic("giardinaggio","alberi da frutto e piante da fiore", 2,	new ArrayList<>());
		topic2.setId(1L);
		fullSession1 = new StudySession(LocalDate.now().plusDays(1), 60, "impara le basi leggendo libri",	new ArrayList<>(List.of(topic2)));
		fullSession1.setId(1L);
		fullSession2 = new StudySession(LocalDate.now().plusDays(3), 120, "acquista terriccio, vasi, semi, ...", new ArrayList<>(List.of(topic2)));
		fullSession2.setId(2L);
		topic3 = new Topic("musica","impara a suonare il pianoforte", 4, new ArrayList<>());
		topic3.setId(3L);
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
	public void testAddSessionToTopicSuccess() {
		Topic topic = new Topic("arte", "rinascimento", 3, new ArrayList<>());
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>());
		topic.addSession(session);
		assertThat(topic.getSessionList()).contains(session);
		assertThat(session.getTopicList()).contains(topic);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddNullSessionToTopicFailure() {
		Topic topic = new Topic("arte","rinascimento",3, new ArrayList<>());
		topic.addSession(null);
	}
	
	@Test
	public void testRemoveSessionFromTopicSuccess() {
		Topic topic = new Topic("arte", "rinascimento", 3, new ArrayList<>());
		topic.setId(5L);
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic,topic2)));
		session.setId(5L);
		assertThat(topic.getSessionList()).contains(session);
		topic.removeSession(session);
		assertThat(topic.getSessionList()).doesNotContain(session);
		assertThat(session.getTopicList()).doesNotContain(topic);
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
	public void testCalculateTotalTimeWithNoSession() {
		topic2.setSessions(new ArrayList<>());
		assertThat(topic2.totalTime()).isZero();
	}
	
	@Test
	public void testCalculateTotalTimeWIthOneSession() {
		topic2.setSessions(new ArrayList<>(List.of(fullSession1)));
		assertThat(topic2.totalTime()).isEqualTo(60);
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
	public void testCalculatePercentageOfTopicCompletionWithNoCompletedSessions() {
		StudySession session3 =	new	StudySession(LocalDate.now().plusDays(1), 60, "nota	1", new	ArrayList<>(List.of(topic3)));
		StudySession session4 = new StudySession(LocalDate.now().plusDays(2), 90, "nota	2", new	ArrayList<>(List.of(topic3)));
		assertThat(topic3.percentageOfCompletion()).isZero();
		assertThat(topic3.getSessionList()).containsExactly(session3,session4);
	}

	@Test
	public void testCalculatePercentageOfTopicCompletionWithAllSessionsCompleted() {
	StudySession session3 = new StudySession(LocalDate.now().plusDays(1), 60, "nota 1", new ArrayList<>(List.of(topic3)));
	session3.complete();
	StudySession session4 = new StudySession(LocalDate.now().plusDays(2), 90, "nota 2", new ArrayList<>(List.of(topic3)));
	session4.complete();
	assertThat(topic3.percentageOfCompletion()).isEqualTo(100);
	}
	
	@Test
	public void testCalculatePercentageOfTopicCompletion() {
		StudySession session = new StudySession(LocalDate.now().plusDays(3), 120, "impara a suonare fra martino", new ArrayList<>(List.of(topic3)));
		session.complete();
		topic3.getSessionList().add(fullSession1);
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
	public void testDecreaseMasteryLevelWithAnInvalidValueFailure() {
		topic2.setMasteryLevel(10);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> topic2.decreaseMasteryLevel(-10));
		assertThat(e.getMessage()).isEqualTo("il valore dei punti da rimuovere deve essere positivo");
	}
	
	@Test
	public void testEqualsWithSameObject() {
		Topic topic = new Topic("A", "B", 1, new ArrayList<>());
		assertThat(topic.equals(topic)).isTrue();
	}

	@Test
	public void testEqualsWithNull() {
		Topic topic = new Topic("A", "B", 1, new ArrayList<>());
		assertThat(topic.equals(null)).isFalse();
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualsWithDifferentClass() {
		Topic topic = new Topic("A", "B", 1, new ArrayList<>());
		assertThat(topic.equals("a string")).isFalse();
	}

	@Test
	public void testEqualsWithDifferentId() {
		Topic topic1 = new Topic("A", "B", 1, new ArrayList<>());
		topic1.setId(1L);
		Topic other = new Topic("A", "B", 1, new ArrayList<>());
		other.setId(2L);
		assertThat(topic1.equals(other)).isFalse();
	}

	@Test
	public void testEqualsWithSameId() {
		Topic topic1 = new Topic("A", "B", 1, new ArrayList<>());
		topic1.setId(1L);
		Topic other = new Topic("C", "D", 2, new ArrayList<>());
		other.setId(1L);
		assertThat(topic1.equals(other)).isTrue();
	}

	@Test
	public void testEqualsWithZeroId() {
		Topic topic1 = new Topic("A", "B", 1, new ArrayList<>());
		Topic other = new Topic("A", "B", 1, new ArrayList<>());
		assertThat(topic1.equals(other)).isFalse();
		assertThat(topic1.getId()).isZero();
	}

	@Test
	public void testHashCodeConsistency() {
		Topic topic = new Topic("A", "B", 1, new ArrayList<>());
		topic.setId(1L);
		int initialHashCode = topic.hashCode();
		assertThat(topic.hashCode()).isEqualTo(initialHashCode);
	}

	@Test
	public void testHashCodeForEqualObjects() {
		Topic topic1 = new Topic("A", "B", 1, new ArrayList<>());
		topic1.setId(1L);
		Topic other = new Topic("C", "D", 2, new ArrayList<>());
		other.setId(1L);
		assertThat(topic1).hasSameHashCodeAs(other);
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
		assertThat(result).contains("Chimica").contains("reazioni chimiche").contains("4").contains("2");
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

	@Test
	public void testGetIdReturnsCorrectValue() {
		Topic topic = new Topic("Test ID", "Descrizione", 1, new ArrayList<>());
		topic.setId(123L);
		assertThat(topic.getId()).isEqualTo(123L);
	}

	@Test
	public void testHashCodeNotZeroForPersistedObject() {
		Topic topic = new Topic("Test HashCode", "Descrizione", 1, new ArrayList<>());
		topic.setId(99L);
		assertThat(topic.hashCode()).isNotZero();
	}
}
