package com.sessionbuilder.e2e;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.core.ComponentMatcher;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.launcher.ApplicationLauncher;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.sessionbuilder.core.EmfFactory;
import com.toedter.calendar.JDateChooser;

import jakarta.persistence.EntityManagerFactory;

@RunWith(GUITestRunner.class)
public class SessionBuilderApplicatione2eIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private static String jdbcUrl;
	
	private static final String TOPIC_FIXTURE_1_NAME = "Collezionismo";
	private static final String TOPIC_FIXTURE_1_DESCRIPTION = "francobolli";
	private static final int TOPIC_FIXTURE_1_DIFFICULTY = 3;
	private static final String TOPIC_FIXTURE_2_NAME = "Corsa";
	private static final String TOPIC_FIXTURE_2_DESCRIPTION = "cento metri scatto";
	private static final int TOPIC_FIXTURE_2_DIFFICULTY = 3;
	private static final LocalDate SESSION_FIXTURE_1_DATE = LocalDate.now().plusDays(1);
	private static final int SESSION_FIXTURE_1_DURATION = 60;
	private static final String SESSION_FIXTURE_1_NOTE = "una nota";
	private static final LocalDate SESSION_FIXTURE_2_DATE = LocalDate.now().plusDays(2);
	private static final int SESSION_FIXTURE_2_DURATION = 90;
	private static final String SESSION_FIXTURE_2_NOTE = "un'altra nota";
	
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
			Map<String, String> schemaProperties = new HashMap<>();
			schemaProperties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
			schemaProperties.put("jakarta.persistence.jdbc.url", jdbcUrl);
			schemaProperties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
			schemaProperties.put("jakarta.persistence.jdbc.password", postgres.getPassword());
			schemaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
			schemaProperties.put("hibernate.hbm2ddl.auto", "create");
			
			schemaProperties.put("hibernate.show_sql", "true");
			schemaProperties.put("hibernate.format_sql", "true");
			schemaProperties.put("jakarta.persistence.validation.mode", "none");

			EntityManagerFactory tempEmf = EmfFactory.createEntityManagerFactory("sessionbuilder-e2e", schemaProperties);
			
			try {
				cleanDatabase();
				addTestSessionToDatabase(TOPIC_FIXTURE_1_NAME, TOPIC_FIXTURE_1_DESCRIPTION, 
					TOPIC_FIXTURE_1_DIFFICULTY, SESSION_FIXTURE_1_DATE, 
					SESSION_FIXTURE_1_DURATION, SESSION_FIXTURE_1_NOTE);
				addTestSessionToDatabase(TOPIC_FIXTURE_2_NAME, TOPIC_FIXTURE_2_DESCRIPTION, 
					TOPIC_FIXTURE_2_DIFFICULTY, SESSION_FIXTURE_2_DATE, 
					SESSION_FIXTURE_2_DURATION, SESSION_FIXTURE_2_NOTE);

				ApplicationLauncher
					.application("com.sessionbuilder.swing.SessionBuilderApplication")
					.withArgs(
						"--postgres-host", postgres.getHost(),
						"--postgres-port", String.valueOf(postgres.getFirstMappedPort()),
						"--db-name", postgres.getDatabaseName(),
						"--db-user", postgres.getUsername(),
						"--db-password", postgres.getPassword(),
						"--persistence-unit", "sessionbuilder-e2e"
					)
					.start();

				robot().waitForIdle();

				window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
					@Override
					protected boolean isMatching(JFrame frame) {
						return frame.isShowing() && frame.getTitle() != null;
					}
				}).withTimeout(20000).using(robot());

				window.show();
				robot().waitForIdle();
				
			} finally {
				if (tempEmf != null && tempEmf.isOpen()) {
					tempEmf.close();
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to launch application", e);
		}
	}

	private void cleanDatabase() {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword())) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("SET session_replication_role = 'replica'");
				
				stmt.executeUpdate("TRUNCATE TABLE topic_studysession CASCADE");
				stmt.executeUpdate("TRUNCATE TABLE studysession CASCADE");
				stmt.executeUpdate("TRUNCATE TABLE topic CASCADE");
				
				stmt.executeUpdate("ALTER SEQUENCE topic_id_seq RESTART WITH 1");
				stmt.executeUpdate("ALTER SEQUENCE studysession_id_seq RESTART WITH 1");
				
				stmt.execute("SET session_replication_role = 'origin'");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to clean database", e);
		}
	}

	@Override
	@After
	public void onTearDown() throws Exception {
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
		
		assertThat(window.list("topicList").contents()).hasSize(2);
		assertThat(window.list("sessionList").contents()).hasSize(2);
		
		assertThat(window.button(JButtonMatcher.withName("deleteTopicButton")).isEnabled()).isFalse();
		assertThat(window.button(JButtonMatcher.withName("deleteSessionButton")).isEnabled()).isFalse();
	}

	@Test
	public void testOnStartAllDatabaseElementsAreShown() {
		assertThat(window.list("topicList").contents()).anySatisfy(e -> assertThat(e).contains(TOPIC_FIXTURE_1_NAME, TOPIC_FIXTURE_1_DESCRIPTION, String.valueOf(TOPIC_FIXTURE_1_DIFFICULTY)));
		assertThat(window.list("topicList").contents()).anySatisfy(e -> assertThat(e).contains(TOPIC_FIXTURE_2_NAME, TOPIC_FIXTURE_2_DESCRIPTION, String.valueOf(TOPIC_FIXTURE_2_DIFFICULTY)));
		assertThat(window.list("sessionList").contents()).anySatisfy(e -> assertThat(e).contains(SESSION_FIXTURE_1_DATE.toString(), String.valueOf(SESSION_FIXTURE_1_DURATION), SESSION_FIXTURE_1_NOTE));
		assertThat(window.list("sessionList").contents()).anySatisfy(e -> assertThat(e).contains(SESSION_FIXTURE_2_DATE.toString(), String.valueOf(SESSION_FIXTURE_2_DURATION), SESSION_FIXTURE_2_NOTE));
	}

	@Test
	public void testAddTopicButtonSuccess() {
		window.button(JButtonMatcher.withName("addTopicNavButton")).click();
		window.textBox("nameField").enterText("Fisica");
		window.textBox("descriptionField").enterText("Meccanica");
		window.textBox("difficultyField").enterText("4");
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		window.button(JButtonMatcher.withName("backButton")).click();
		
		assertThat(window.list("topicList").contents()).anySatisfy(e -> 
			assertThat(e).contains("Fisica", "Meccanica", "4"));
	}

	@Test
	public void testAddTopicButtonError() {
		window.button(JButtonMatcher.withName("addTopicNavButton")).click();
		window.textBox("nameField").enterText(TOPIC_FIXTURE_1_NAME);
		window.textBox("descriptionField").enterText(TOPIC_FIXTURE_1_DESCRIPTION);
		window.textBox("difficultyField").enterText(String.valueOf(TOPIC_FIXTURE_1_DIFFICULTY));
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		
		String errorText = window.label(JLabelMatcher.withName("errorTopicPanelLbl")).text();
		assertThat(errorText).contains("Errore nel salvare il topic:");
	}

	@Test
	public void testAddSessionButtonSuccess() {
		window.button(JButtonMatcher.withName("addSessionNavButton")).click();
		
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			LocalDate futureDate = LocalDate.now().plusDays(5);
			Date javaDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(javaDate);
		});
		
		window.textBox("durationField").enterText("120");
		window.textBox("noteField").enterText("Nuova sessione");
		window.list("sessionPanelTopicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		window.button(JButtonMatcher.withName("backSessionButton")).click();
		
		assertThat(window.list("sessionList").contents()).anySatisfy(e -> 
			assertThat(e).contains("120", "Nuova sessione"));
	}

	@Test
	public void testAddSessionButtonError() {
		window.button(JButtonMatcher.withName("addSessionNavButton")).click();
		
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			Date pastDate = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(pastDate);
		});
		
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("Sessione passata");
		window.list("sessionPanelTopicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		
		String errorText = window.label(JLabelMatcher.withName("sessionErrorMessage")).text();
		assertThat(errorText).contains("Errore nel salvare la sessione:");
	}

	@Test
	public void testAddSessionButtonErrorDuplicate() {
		window.button(JButtonMatcher.withName("addSessionNavButton")).click();
		
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			Date javaDate = Date.from(SESSION_FIXTURE_1_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(javaDate);
		});
		
		window.textBox("durationField").enterText(String.valueOf(SESSION_FIXTURE_1_DURATION));
		window.textBox("noteField").enterText(SESSION_FIXTURE_1_NOTE);
		window.list("sessionPanelTopicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		
		String errorText = window.label(JLabelMatcher.withName("sessionErrorMessage")).text();
		assertThat(errorText).contains("Errore nel salvare la sessione:");
	}

	@Test
	public void testDeleteSessionButtonSuccess() {
		window.list("sessionList").selectItem(Pattern.compile(".*" + SESSION_FIXTURE_1_NOTE + ".*"));
		
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		
		assertThat(window.list("sessionList").contents()).hasSize(1);
		assertThat(window.list("sessionList").contents()).doesNotContain(
			SESSION_FIXTURE_1_DATE.toString(), String.valueOf(SESSION_FIXTURE_1_DURATION), SESSION_FIXTURE_1_NOTE);
	}

	@Test
	public void testDeleteSessionButtonError() {
		window.list("sessionList").selectItem(Pattern.compile(".*" + SESSION_FIXTURE_1_NOTE + ".*"));
		removeTestSessionFromDatabase(SESSION_FIXTURE_1_NOTE);
		window.button(JButtonMatcher.withName("deleteSessionButton")).click();
		
		String errorText = window.label(JLabelMatcher.withName("errorMessageLabel")).text();
		assertThat(errorText).contains("Error");
	}

	@Test
	public void testDeleteTopicButtonSuccess() {
		window.list("topicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		assertThat(window.list("topicList").contents()).hasSize(1);
		assertThat(window.list("topicList").contents()).doesNotContain(
			TOPIC_FIXTURE_1_NAME, TOPIC_FIXTURE_1_DESCRIPTION, String.valueOf(TOPIC_FIXTURE_1_DIFFICULTY));
	}

	@Test
	public void testDeleteTopicButtonError() {
		window.list("topicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		removeTestTopicFromDatabase(TOPIC_FIXTURE_1_NAME);
		window.button(JButtonMatcher.withName("deleteTopicButton")).click();
		
		String errorText = window.label(JLabelMatcher.withName("errorMessageLabel")).text();
		assertThat(errorText).contains("Topic non trovato");
	}

	@Test
	public void testCompleteSessionButtonSuccess() {
		window.list("sessionList").selectItem(Pattern.compile(".*" + SESSION_FIXTURE_1_NOTE + ".*"));
		window.button(JButtonMatcher.withName("completeSessionButton")).click();
		assertThat(window.list("sessionList").contents()[0]).contains("Completed: true");
	}

	@Test
	public void testCompleteSessionButtonError() {
		window.list("sessionList").clearSelection();
		window.button(JButtonMatcher.withName("completeSessionButton")).requireDisabled();
		assertThat(window.list("sessionList").contents()).hasSize(2);
	}

	@Test
	public void testTotalTimeButtonSuccess() {
		window.list("topicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withText("totalTime")).click();
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText("Tempo totale: 60 minuti");
		assertThat(window.button(JButtonMatcher.withText("totalTime")).isEnabled()).isTrue();
		assertThat(window.label(JLabelMatcher.withName("errorMessageLabel")).text()).isEqualTo("Tempo totale: 60 minuti");
	}

	@Test
	public void testTotalTimeButtonError() {
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withText("totalTime")).requireDisabled();
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText(" ");
		assertThat(window.button(JButtonMatcher.withText("totalTime")).isEnabled()).isFalse();
		assertThat(window.label(JLabelMatcher.withName("errorMessageLabel")).text()).isEqualTo(" ");
	}

	@Test
	public void testPercentageButtonSuccess() {
		window.list("topicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withText("%Completion")).click();
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText("Percentuale di completamento: 0%");
		assertThat(window.button(JButtonMatcher.withText("%Completion")).isEnabled()).isTrue();
		assertThat(window.label(JLabelMatcher.withName("errorMessageLabel")).text()).isEqualTo("Percentuale di completamento: 0%");
	}

	@Test
	public void testPercentageButtonError() {
		window.list("topicList").clearSelection();
		window.button(JButtonMatcher.withText("%Completion")).requireDisabled();
		window.label(JLabelMatcher.withName("errorMessageLabel")).requireText(" ");
		assertThat(window.button(JButtonMatcher.withText("%Completion")).isEnabled()).isFalse();
		assertThat(window.label(JLabelMatcher.withName("errorMessageLabel")).text()).isEqualTo(" ");
	}

	@Test
	public void testTopicPanelAddButtonSuccess() {
		window.button(JButtonMatcher.withName("addTopicNavButton")).click();
		
		window.textBox("nameField").enterText("Geografia");
		window.textBox("descriptionField").enterText("Capitali europee");
		window.textBox("difficultyField").enterText("2");
		window.button(JButtonMatcher.withName("addTopicButton")).click();
		window.button(JButtonMatcher.withName("backButton")).click();
		
		assertThat(window.list("topicList").contents()).anySatisfy(e -> 
			assertThat(e).contains("Geografia", "Capitali europee", "2"));
	}

	@Test
	public void testSessionPanelAddButtonSuccess() {
		window.button(JButtonMatcher.withName("addSessionNavButton")).click();
		
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			LocalDate futureDate = LocalDate.now().plusDays(7);
			Date javaDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(javaDate);
		});
		
		window.textBox("durationField").enterText("75");
		window.textBox("noteField").enterText("Sessione di prova");
		window.list("sessionPanelTopicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		window.button(JButtonMatcher.withName("backSessionButton")).click();
		
		assertThat(window.list("sessionList").contents()).anySatisfy(e -> 
			assertThat(e).contains("75", "Sessione di prova"));
	}

	@Test
	public void testSessionPanelAddButtonError() {
		window.button(JButtonMatcher.withName("addSessionNavButton")).click();
		GuiActionRunner.execute(() -> {
			ComponentMatcher matcher = new GenericTypeMatcher<JDateChooser>(JDateChooser.class) {
				@Override
				protected boolean isMatching(JDateChooser component) {
					return "dateChooser".equals(component.getName());
				}
			};
			JDateChooser dateChooser = (JDateChooser) robot().finder().find(matcher);
			Date pastDate = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
			dateChooser.setDate(pastDate);
		});
		
		window.textBox("durationField").enterText("60");
		window.textBox("noteField").enterText("Sessione passata");
		window.list("sessionPanelTopicList").selectItem(Pattern.compile(".*" + TOPIC_FIXTURE_1_NAME + ".*"));
		window.button(JButtonMatcher.withName("addSessionButton")).click();
		String errorText = window.label(JLabelMatcher.withName("sessionErrorMessage")).text();
		assertThat(errorText).contains("Errore nel salvare la sessione:");
	}


	private void addTestSessionToDatabase(String topicName, String description, int difficulty, 
			LocalDate date, int duration, String note) {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword())) {
			
			Long topicId = findTopicIdByName(conn, topicName);
			if (topicId == null) {
				topicId = insertTopic(conn, topicName, description, difficulty);
			}
			
			long sessionId = insertSession(conn, date, duration, note);
			
			insertRelation(conn, topicId, sessionId);
			
		} catch (SQLException e) {
			throw new RuntimeException("Failed to insert test data", e);
		}
	}

	private Long findTopicIdByName(Connection conn, String topicName) throws SQLException {
		String sql = "SELECT id FROM topic WHERE name = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, topicName);
			try (var rs = stmt.executeQuery()) {
				return rs.next() ? rs.getLong(1) : null;
			}
		}
	}

	private Long insertTopic(Connection conn, String name, String description, int difficulty) throws SQLException {
		String sql = "INSERT INTO topic (name, description, difficulty, masterylevel) VALUES (?, ?, ?, 0)";
		try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, difficulty);
			stmt.executeUpdate();
			
			try (var rs = stmt.getGeneratedKeys()) {
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	private Long insertSession(Connection conn, LocalDate date, int duration, String note) throws SQLException {
		String sql = "INSERT INTO studysession (date, duration, note, iscomplete) VALUES (?, ?, ?, false)";
		try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
			stmt.setDate(1, java.sql.Date.valueOf(date));
			stmt.setInt(2, duration);
			stmt.setString(3, note);
			stmt.executeUpdate();
			
			try (var rs = stmt.getGeneratedKeys()) {
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	private void insertRelation(Connection conn, long topicId, long sessionId) throws SQLException {
		String sql = "INSERT INTO topic_studysession (topic_id, session_id) VALUES (?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, topicId);
			stmt.setLong(2, sessionId);
			stmt.executeUpdate();
		}
	}

	private void removeTestTopicFromDatabase(String name) {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword())) {
			Long topicId = findTopicIdByName(conn, name);
			if (topicId == null) return;

			String deleteRelations = "DELETE FROM topic_studysession WHERE topic_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(deleteRelations)) {
				stmt.setLong(1, topicId);
				stmt.executeUpdate();
			}

			String deleteTopic = "DELETE FROM topic WHERE id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(deleteTopic)) {
				stmt.setLong(1, topicId);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to remove test topic", e);
		}
	}

	private void removeTestSessionFromDatabase(String note) {
		try (Connection conn = DriverManager.getConnection(jdbcUrl, postgres.getUsername(), postgres.getPassword())) {
			Long sessionId = findSessionIdByNote(conn, note);
			if (sessionId == null) return;

			String deleteRelations = "DELETE FROM topic_studysession WHERE session_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(deleteRelations)) {
				stmt.setLong(1, sessionId);
				stmt.executeUpdate();
			}

			String deleteSession = "DELETE FROM studysession WHERE id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(deleteSession)) {
				stmt.setLong(1, sessionId);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to remove test session", e);
		}
	}

	private Long findSessionIdByNote(Connection conn, String note) throws SQLException {
		String query = "SELECT id FROM studysession WHERE note = ?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, note);
			try (var rs = stmt.executeQuery()) {
				return rs.next() ? rs.getLong(1) : null;
			}
		}
	}
}