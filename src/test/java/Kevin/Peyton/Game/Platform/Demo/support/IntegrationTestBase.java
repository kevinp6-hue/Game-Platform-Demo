package Kevin.Peyton.Game.Platform.Demo.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for full integration tests that need:
 * - a real Postgres database (Testcontainers)
 * - Flyway migrations applied
 * - an HTTP client against a RANDOM_PORT SpringBootTest server
 *
 * Keep controller/service/repository tests in their own packages and extend this
 * class only for true end-to-end integration tests.
 *
 * Uses the Singleton Container Pattern: one PostgreSQL container is started for
 * the entire JVM session and shared across all subclasses.  This keeps the
 * datasource URL stable so Spring's test-context cache works correctly — a
 * per-class container is stopped after the first test class finishes, leaving
 * the cached context pointing at a dead port for every subsequent class.
 */
public abstract class IntegrationTestBase {

	// Singleton container — started once, never stopped between test classes.
	// Ryuk (or JVM shutdown hooks) cleans up the Docker container at process exit.
	protected static final PostgreSQLContainer<?> postgres =
			new PostgreSQLContainer<>("postgres:15");

	static {
		postgres.start();
	}

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		registry.add("spring.flyway.url", postgres::getJdbcUrl);
		registry.add("spring.flyway.user", postgres::getUsername);
		registry.add("spring.flyway.password", postgres::getPassword);
	}

	@LocalServerPort
	protected int port;

	protected RestClient restClient;

	@BeforeEach
	protected void baseSetUp() {
		restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

		var flyway = org.flywaydb.core.Flyway.configure()
				.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
				.locations("classpath:db/migration")
				.cleanDisabled(false)
				.load();

		flyway.clean();
		flyway.migrate();
	}
}
