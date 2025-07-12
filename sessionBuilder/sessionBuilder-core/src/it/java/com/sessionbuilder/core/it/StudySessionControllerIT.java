package com.sessionbuilder.core.it;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.sessionbuilder.core.backend.SessionViewCallback;
import com.sessionbuilder.core.backend.StudySession;
import com.sessionbuilder.core.backend.StudySessionController;
import com.sessionbuilder.core.backend.StudySessionInterface;
import com.sessionbuilder.core.backend.StudySessionRepository;
import com.sessionbuilder.core.backend.StudySessionRepositoryInterface;
import com.sessionbuilder.core.backend.StudySessionService;
import com.sessionbuilder.core.backend.Topic;
import com.sessionbuilder.core.backend.TopicRepository;
import com.sessionbuilder.core.backend.TopicRepositoryInterface;
import com.sessionbuilder.core.backend.TransactionManager;
import com.sessionbuilder.core.backend.TransactionManagerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StudySessionControllerIT extends BaseBackendIntegrationTest {
	private StudySessionController sessionController;
	@Mock
	private SessionViewCallback viewCallback;
	private StudySessionInterface sessionService;

	@Override
	protected AbstractModule getTestSpecificModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(TopicRepositoryInterface.class).to(TopicRepository.class).in(Singleton.class);
				bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class).in(Singleton.class);
				bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
				bind(StudySessionInterface.class).to(StudySessionService.class).in(Singleton.class);
				bind(SessionViewCallback.class).toInstance(viewCallback);
			}
		};
	}

	@Override
	protected void onSetup() {
		sessionController = injector.getInstance(StudySessionController.class);
		sessionService = injector.getInstance(StudySessionInterface.class);
	}

	@Test
	public void handleCreateSessionIt() {
		Topic topic = new Topic("Matematica", "Algebra lineare", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		ArrayList<Long> topicsIds = new ArrayList<>(List.of(topic.getId()));
		StudySession session = sessionController.handleCreateSession(
			LocalDate.now().plusDays(1), 90, "sessione di algebra", topicsIds);
		verify(viewCallback).onSessionAdded(session);
		verify(viewCallback, never()).onSessionError(anyString());
		assertThat(session).isNotNull();
		assertThat(session.getId()).isPositive();
		assertThat(session.getDate()).isEqualTo(LocalDate.now().plusDays(1));
		assertThat(session.getDuration()).isEqualTo(90);
		assertThat(session.getNote()).isEqualTo("sessione di algebra");
		assertThat(session.getTopicList()).contains(topic);
		assertThat(sessionService.getSessionById(session.getId())).isEqualTo(session);
	}

	@Test
	public void handleGetSessionIt() {
		Topic topic = new Topic("Biologia", "Genetica", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		ArrayList<Long> topics = new ArrayList<>(List.of(topic.getId()));
		StudySession createdSession = sessionService.createSession(
			LocalDate.now().plusDays(1), 75, "studio genetica", topics);
		StudySession retrievedSession = sessionController.handleGetSession(createdSession.getId());
		assertThat(retrievedSession).isNotNull().isEqualTo(createdSession);
		assertThat(retrievedSession.getNote()).isEqualTo("studio genetica");
		verify(viewCallback, never()).onSessionError(anyString());
	}

	@Test
	public void handleDeleteSessionIt() {
		Topic topic = new Topic("Filosofia", "Etica", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		ArrayList<Long> topics = new ArrayList<>(List.of(topic.getId()));
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 60, "sessione di etica", topics);
		long sessionId = session.getId();
		sessionController.handleDeleteSession(sessionId);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> sessionService.getSessionById(sessionId));
		assertThat(exception.getMessage()).isEqualTo("non esiste una session con tale id");
		verify(viewCallback).onSessionRemoved(session);
		verify(viewCallback, never()).onSessionError(anyString());
	}

	@Test
	public void handleAddTopicIt() {
		Topic topic1 = new Topic("Letteratura", "Dante", 3, new ArrayList<>());
		Topic topic2 = new Topic("Arte", "Rinascimento", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic1);
			repo.save(topic2);
			return null;
		});
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 120, "sessione multidisciplinare", new ArrayList<>(List.of(topic1.getId())));
		sessionController.handleAddTopic(session.getId(), topic2.getId());
		StudySession updatedSession = sessionService.getSessionById(session.getId());
		assertThat(updatedSession.getTopicList()).contains(topic1, topic2);
		verify(viewCallback, never()).onSessionError(anyString());
	}

	@Test
	public void handleRemoveTopicIt() {
		Topic topic1 = new Topic("Economia", "Microeconomia", 4, new ArrayList<>());
		Topic topic2 = new Topic("Diritto", "Costituzionale", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic1);
			repo.save(topic2);
			return null;
		});
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 90, "sessione economico-giuridica", new ArrayList<>(List.of(topic1.getId(), topic2.getId())));
		sessionController.handleRemoveTopic(session.getId(), topic2.getId());
		StudySession updatedSession = sessionService.getSessionById(session.getId());
		assertThat(updatedSession.getTopicList()).containsExactly(topic1);
		verify(viewCallback, never()).onSessionError(anyString());
	}

	@Test
	public void handleCompleteSessionSuccessIt() {
		Topic topic = new Topic("Psicologia", "Cognitivismo", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		ArrayList<Long> topics = new ArrayList<>(List.of(topic.getId()));
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 120, "sessione di psicologia", topics);
		assertThat(session.isComplete()).isFalse();
		sessionController.handleCompleteSession(session.getId());
		StudySession completedSession = sessionService.getSessionById(session.getId());
		assertThat(completedSession.isComplete()).isTrue();
		verify(viewCallback, never()).onSessionError(anyString());
	}

	@Test
	public void handleCompleteAlreadyCompletedSessionFailureIt() {
		Topic topic = new Topic("Sociologia", "Teorie sociali", 3, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic);
			return null;
		});
		ArrayList<Long> topics = new ArrayList<>(List.of(topic.getId()));
		StudySession session = sessionService.createSession(
			LocalDate.now().plusDays(1), 90, "sessione di sociologia", topics);
		sessionController.handleCompleteSession(session.getId());
		sessionController.handleCompleteSession(session.getId());
		verify(viewCallback).onSessionError(anyString());
	}

	@Test
	public void completeWorkflowIt() {
		Topic topic1 = new Topic("Informatica", "Algoritmi", 5, new ArrayList<>());
		Topic topic2 = new Topic("Matematica", "Calcolo", 4, new ArrayList<>());
		transactionManager.doInTopicTransaction(repo -> {
			repo.save(topic1);
			repo.save(topic2);
			return null;
		});
		StudySession session = sessionController.handleCreateSession(
			LocalDate.now().plusDays(1), 150, "sessione algoritmi e calcolo", new ArrayList<>(List.of(topic1.getId())));
		long sessionId = session.getId();
		sessionController.handleAddTopic(sessionId, topic2.getId());
		StudySession sessionWithBothTopics = sessionController.handleGetSession(sessionId);
		assertThat(sessionWithBothTopics.getTopicList()).hasSize(2);
		assertThat(sessionWithBothTopics.getTopicList()).contains(topic1, topic2);
		sessionController.handleCompleteSession(sessionId);
		StudySession completedSession = sessionController.handleGetSession(sessionId);
		assertThat(completedSession.isComplete()).isTrue();
		sessionController.handleDeleteSession(sessionId);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> sessionController.handleGetSession(sessionId));
		assertThat(exception.getMessage()).isEqualTo("non esiste una session con tale id");
	}
}