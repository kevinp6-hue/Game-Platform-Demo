package Kevin.Peyton.Game.Platform.Demo.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Testcontainers;

import Kevin.Peyton.Game.Platform.Demo.GamePlatformDemoApplication;
import Kevin.Peyton.Game.Platform.Demo.support.IntegrationTestBase;

@ActiveProfiles("test")
@SpringBootTest(classes = GamePlatformDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class LibraryControllerIT extends IntegrationTestBase {

    @Test
    void getMyLibrary_unauthenticated_returns401() {
        assertThatThrownBy(() -> restClient.get()
                .uri("/API/me/library")
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void updateLibraryEntry_unauthenticated_returns401() {
        assertThatThrownBy(() -> restClient.patch()
                .uri("/API/me/library/1")
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }
}
