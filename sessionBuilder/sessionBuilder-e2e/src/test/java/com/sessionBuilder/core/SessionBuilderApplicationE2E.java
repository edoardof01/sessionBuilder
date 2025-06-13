package com.sessionBuilder.core;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JFrame;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.launcher.ApplicationLauncher;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@RunWith(GUITestRunner.class)
public class SessionBuilderApplicationE2E extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private static String jdbcUrl;

	@SuppressWarnings("resource")
	@ClassRule
	public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName("sessionbuilder_e2e")
			.withUsername("test_user")
			.withPassword("test_password")
			.withExposedPorts(5432)
			.waitingFor(Wait.forListeningPort());

	@BeforeClass
	public static void setUpContainer() {
		postgres.start();
		System.out.println("PostgreSQL container started on port: " + postgres.getFirstMappedPort());
		
		jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", 
			postgres.getHost(), postgres.getFirstMappedPort(), postgres.getDatabaseName());
	}

	@Override
	protected void onSetUp() {
		try {
			ApplicationLauncher
				.application("com.sessionBuilder.swing.SessionBuilderApplication")
				.withArgs(
					"--postgres-host", postgres.getHost(),
					"--postgres-port", String.valueOf(postgres.getFirstMappedPort()),
					"--db-name", postgres.getDatabaseName(),
					"--db-user", postgres.getUsername(),
					"--db-password", postgres.getPassword()
				)
				.start();

			Thread.sleep(3000);

			window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
				@Override
				protected boolean isMatching(JFrame frame) {
					return frame.isShowing() && frame.getTitle() != null;
				}
			}).withTimeout(10000).using(robot());

			window.show();
			robot().waitForIdle();

			addTestSessionToDatabase("Collezionismo", "francobolli", 3, LocalDate.now().plusDays(1), 60, "una nota");
			addTestSessionToDatabase("Corsa", "cento metri scatto", 3, LocalDate.now().plusDays(2), 90, "un'altra nota");

		} catch (Exception e) {
			throw new RuntimeException("Failed to launch application", e);
		}
	}

	@Override
	protected void onTearDown() throws Exception {
		if (window != null) {
			window.cleanUp();
		}
	}

	@Test
	public void applicationShouldStart() {
		assertThat(window).isNotNull();
		window.requireVisible();
		
		window.list("topicList").requireVisible();
		window.list("sessionList").requireVisible();
		window.button(JButtonMatcher.withName("deleteTopicButton")).requireVisible().requireDisabled();
		window.button(JButtonMatcher.withName("deleteSessionButton")).requireVisible().requireDisabled();
	}

	@Test
	public void testOnStartAllDatabaseElementsAreShown() {
		assertThat(window.list("topicList").contents()).anySatisfy(e -> assertThat(e).contains("Collezionismo", "francobolli"));
		assertThat(window.list("topicList").contents()).anySatisfy(e -> assertThat(e).contains("Corsa", "cento metri scatto"));
		assertThat(window.list("sessionList").contents()).anySatisfy(e -> assertThat(e).contains(LocalDate.now().plusDays(1).toString(), String.valueOf(60), "una nota"));
		assertThat(window.list("sessionList").contents()).anySatisfy(e -> assertThat(e).contains(LocalDate.now().plusDays(2).toString(), String.valueOf(90), "un'altra nota"));
	}

	private void addTestSessionToDatabase(String topicName, String description, int difficulty, 
			LocalDate date, int duration, String note) {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword())) {
			
			String insertTopic = "INSERT INTO Topic (name, description, difficulty, masterylevel) VALUES (?, ?, ?, 0)";
			try (PreparedStatement topicStmt = conn.prepareStatement(insertTopic, PreparedStatement.RETURN_GENERATED_KEYS)) {
				topicStmt.setString(1, topicName);
				topicStmt.setString(2, description);
				topicStmt.setInt(3, difficulty);
				topicStmt.executeUpdate();
				
				var rs = topicStmt.getGeneratedKeys();
				rs.next();
				long topicId = rs.getLong(1);
				
				String insertSession = "INSERT INTO StudySession (date, duration, note, iscomplete) VALUES (?, ?, ?, false)";
				try (PreparedStatement sessionStmt = conn.prepareStatement(insertSession, PreparedStatement.RETURN_GENERATED_KEYS)) {
					sessionStmt.setDate(1, java.sql.Date.valueOf(date));
					sessionStmt.setInt(2, duration);
					sessionStmt.setString(3, note);
					sessionStmt.executeUpdate();
					
					var sessionRs = sessionStmt.getGeneratedKeys();
					sessionRs.next();
					long sessionId = sessionRs.getLong(1);
					
					String insertRelation = "INSERT INTO StudySession_Topic (sessionList_id, topicList_id) VALUES (?, ?)";
					try (PreparedStatement relationStmt = conn.prepareStatement(insertRelation)) {
						relationStmt.setLong(1, sessionId);
						relationStmt.setLong(2, topicId);
						relationStmt.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to insert test data", e);
		}
	}
}