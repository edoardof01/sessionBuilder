package com.sessionBuilder.swing;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sessionBuilder.core.*;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
	name = "sessionbuilder", 
	mixinStandardHelpOptions = true, 
	version = "SessionBuilder 1.0",
	description = "Study Session Builder - Gestione sessioni di studio"
)
public class SessionBuilderApplication implements Callable<Integer> {
	
	private static final Logger logger = LogManager.getLogger(SessionBuilderApplication.class);

	@Option(names = {"--postgres-host"}, description = "PostgreSQL host (default: ${DEFAULT-VALUE})", defaultValue = "localhost")
	private String postgresHost;

	@Option(names = {"--postgres-port"}, description = "PostgreSQL port (default: ${DEFAULT-VALUE})", defaultValue = "5432")
	private int postgresPort;

	@Option(names = {"--db-name"}, description = "Database name (default: ${DEFAULT-VALUE})", defaultValue = "test")
	private String databaseName;

	@Option(names = {"--db-user"}, description = "Database user (default: ${DEFAULT-VALUE})", defaultValue = "test")
	private String username;

	@Option(names = {"--db-password"}, description = "Database password (default: ${DEFAULT-VALUE})", defaultValue = "test")
	private String password;

	@Option(names = {"--persistence-unit"}, description = "JPA persistence unit name (default: ${DEFAULT-VALUE})", defaultValue = "sessionbuilder-prod")
	private String persistenceUnit;

	public static void main(String[] args) {
		int exitCode = new CommandLine(new SessionBuilderApplication()).execute(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	@Override
	public Integer call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				String finalHost = getEnvOrDefault("DB_HOST", postgresHost);
				int finalPort = Integer.parseInt(getEnvOrDefault("DB_PORT", String.valueOf(postgresPort)));
				String finalDbName = getEnvOrDefault("POSTGRES_TEST_DB", databaseName);
				String finalUsername = getEnvOrDefault("POSTGRES_TEST_USER", username);
				String finalPassword = getEnvOrDefault("POSTGRES_TEST_PASSWORD", password);
				
				Map<String, String> properties = new HashMap<>();
				String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", finalHost, finalPort, finalDbName);
				
				properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
				properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
				properties.put("jakarta.persistence.jdbc.user", finalUsername);
				properties.put("jakarta.persistence.jdbc.password", finalPassword);
				properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
				properties.put("hibernate.show_sql", "true");
				properties.put("hibernate.format_sql", "true");
				properties.put("hibernate.connection.pool_size", "5");
				properties.put("hibernate.cache.use_second_level_cache", "false");
				properties.put("hibernate.cache.use_query_cache", "false");
				
				logger.info("Database: {}", jdbcUrl);
				logger.info("Using host: {}, port: {}, db: {}, user: {}", finalHost, finalPort, finalDbName, finalUsername);
				
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
				
				Injector injector = Guice.createInjector(new AbstractModule() {
					@Override
					protected void configure() {
						bind(EntityManagerFactory.class).toInstance(emf);
						bind(StudySessionRepositoryInterface.class).to(StudySessionRepository.class);
						bind(TopicRepositoryInterface.class).to(TopicRepository.class);
						bind(TransactionManager.class).to(TransactionManagerImpl.class);
						bind(TopicServiceInterface.class).to(TopicService.class);
						bind(TopicViewCallback.class).to(TopicAndSessionManager.class);
						bind(SessionViewCallback.class).to(TopicAndSessionManager.class);
						bind(StudySessionInterface.class).to(StudySessionService.class);
						bind(TopicViewCallback.class).to(TopicAndSessionManager.class);
						
					}
				});
				
				TopicController topicController = injector.getInstance(TopicController.class);
				StudySessionController sessionController = injector.getInstance(StudySessionController.class);
				
				TopicAndSessionManager frame = new TopicAndSessionManager();
				frame.setTopicController(topicController);
				frame.setSessionController(sessionController);
				
				frame.getTopicPanel().setTopicController(topicController);
				frame.getTopicPanel().setManagerView(frame);
				frame.getSessionPanel().setSessionController(sessionController);
				frame.getSessionPanel().setManagerView(frame);
				
				frame.setVisible(true);
				
				logger.info("SessionBuilder avviato con successo!");
				logger.info("Database: {}", jdbcUrl);
				
			} catch (Exception e) {
				logger.error("Errore durante l'avvio dell'applicazione: {}", e.getMessage(), e);
				System.exit(1);
			}
		});
		return 0;
	}
	
	private String getEnvOrDefault(String envVar, String defaultValue) {
		String value = System.getenv(envVar);
		if (value != null && !value.trim().isEmpty()) {
			logger.info("Using environment variable {}: {}", envVar, value);
			return value;
		}
		logger.info("Using default value for {}: {}", envVar, defaultValue);
		return defaultValue;
	}
}