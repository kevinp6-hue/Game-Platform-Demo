package Kevin.Peyton.Game.Platform.Demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import Kevin.Peyton.Game.Platform.Demo.dto.games.GameResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.games.GameCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.repository.DeveloperRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.Developer;

import Kevin.Peyton.Game.Platform.Demo.GamePlatformDemoApplication;

@SpringBootTest(classes = GamePlatformDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.flyway.clean-disabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
    })
@Testcontainers
class GamesControllerIT {

	

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		// Make Flyway use the same Testcontainers DB as the datasource (removes any ambiguity)
		registry.add("spring.flyway.url", postgres::getJdbcUrl);
		registry.add("spring.flyway.user", postgres::getUsername);
		registry.add("spring.flyway.password", postgres::getPassword);
	}

	@LocalServerPort
	private int port;

	private RestClient restClient;

	private final DeveloperRepository developerRepository;
	private Integer testDeveloperId;

	/**
	 * Constructor for GamesControllerIT.
	 * This constructor is annotated with @Autowired, which means that Spring will automatically inject the DeveloperRepository bean when creating an instance of this test class. 
	 * The DeveloperRepository is used to interact with the database for developer-related operations during the tests. By using constructor injection, we can ensure that the DeveloperRepository is properly initialized and available for use in the test methods.
	 */
	@Autowired
	public GamesControllerIT(
			DeveloperRepository developerRepository) {
		this.developerRepository = developerRepository;
	}

	/**
	 * Setup method to initialize the RestClient before each test.
	 * This method is annotated with @BeforeEach, which means it will be executed before each test method in this class. 
	 * It initializes the RestClient with the base URL of the application, which includes the random port assigned to the application during testing.
	 * By setting up the RestClient in this way, we can easily send HTTP requests to the application endpoints in our test methods without having to hardcode the base URL or port number.
	 */
	@BeforeEach
	void setUp() {
		restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

		// Reset schema/data for each test (safe because this is a throwaway Testcontainers database).
		var flyway = org.flywaydb.core.Flyway.configure()
    	.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
    	.locations("classpath:db/migration")
    	.cleanDisabled(false)
    	.load();

		flyway.clean();
		flyway.migrate();

		// Create a test developer and retain its generated ID for requests
		Developer developer = new Developer();
		developer.setDevName("Test Developer");
		developer.setCountry("USA");
		testDeveloperId = developerRepository.save(developer).getId();
	}


	/**
	 * Test to verify that the application context loads successfully.
	 * This test checks that the Spring application context can be loaded without any issues.
	 * If the context fails to load, this test will fail, indicating that there may be configuration issues or missing dependencies in the application.
	 */
	@Test
	void contextLoads() {
	}

	/**
	 * Test to verify that the GET /API/games endpoint returns a successful response.
	 * This test sends a GET request to the endpoint and checks that the response status code is 200 OK.
	 */
	@Test
	void testGetGame() {
		ResponseEntity<GameResponse[]> response = restClient.get()
				.uri("/API/games")
				.retrieve()
				.toEntity(GameResponse[].class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
	}

	/**
	 * Test to verify that the POST /API/games endpoint successfully creates a new game.
	 * This test sends a POST request with a GameCreateRequest payload to the endpoint and checks that the response status code is 200 OK.
	 * It also verifies that the response body contains the expected game title, confirming that the game was created successfully.
	 */
	@Test
	void testCreateGame() {
		GameCreateRequest newGame = new GameCreateRequest(
				"Test Game",
				LocalDate.of(2024, 1, 1),
				BigDecimal.valueOf(59.99),
				testDeveloperId
		);

		ResponseEntity<GameResponse> response = restClient.post()
				.uri("/API/games")
				.body(newGame)
				.retrieve()
				.toEntity(GameResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().title()).isEqualTo("Test Game");
		assertThat(response.getBody().developerId()).isEqualTo(testDeveloperId);
	}
	

}

