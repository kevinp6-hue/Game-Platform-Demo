package Kevin.Peyton.Game.Platform.Demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import Kevin.Peyton.Game.Platform.Demo.support.IntegrationTestBase;

@ActiveProfiles("test")
@SpringBootTest(classes = GamePlatformDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContextLoadsIT extends IntegrationTestBase {
	/**
	 * Test to verify that the application context loads successfully.
	 * This test checks that the Spring application context can be loaded without any issues.
	 * If the context fails to load, this test will fail, indicating that there may be configuration issues or missing dependencies in the application.
	 */
	@Test
	void contextLoads() {
	}
}
