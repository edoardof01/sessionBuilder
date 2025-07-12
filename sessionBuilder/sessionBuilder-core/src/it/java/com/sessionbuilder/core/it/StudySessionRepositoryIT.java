package com.sessionbuilder.core.it;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sessionbuilder.core.backend.StudySession;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;
import org.junit.Test;

public class StudySessionRepositoryIT extends BaseBackendIntegrationTest {

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
			}
		};
	}

	@Test
	public void testFindByIdFailureIt() {
		long id = 10000000;
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> transactionManager.doInSessionTransaction(repo -> repo.findById(id)));
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}

	@Test
	public void testSaveAndFindSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		long sessionId = session.getId();
		assertThat(sessionId).isPositive();
		StudySession retrievedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(retrievedSession).isNotNull();
		assertThat(retrievedSession.getNote()).isEqualTo("una nota");
		assertThat(retrievedSession.getDuration()).isEqualTo(60);
	}

	@Test
	public void testUpdateSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		long sessionId = session.getId();
		StudySession retrievedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(retrievedSession.getDuration()).isEqualTo(60);
		retrievedSession.setDuration(90);
		transactionManager.doInSessionTransaction(repo -> {
			repo.update(retrievedSession);
			return null;
		});
		StudySession updatedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(updatedSession.getDuration()).isEqualTo(90);
	}

	@Test
	public void testDeleteSessionIt() {
		Topic topic = new Topic("Corsa", "allena lo scatto", 1, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		StudySession session = new StudySession(LocalDate.now().plusDays(1), 60, "una nota", new ArrayList<>(List.of(topic)));
		transactionManager.doInSessionTransaction(repo -> {
			repo.save(session);
			return null;
		});
		long sessionId = session.getId();
		StudySession retrievedSession = transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		assertThat(retrievedSession).isNotNull();
		transactionManager.doInSessionTransaction(repo -> {
			repo.delete(sessionId);
			return null;
		});
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			transactionManager.doInSessionTransaction(repo -> repo.findById(sessionId));
		});
		assertThat(e.getMessage()).isEqualTo("non esiste una session con tale id");
	}
}