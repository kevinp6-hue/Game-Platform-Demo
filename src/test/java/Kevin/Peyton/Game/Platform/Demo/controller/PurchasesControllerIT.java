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
class PurchasesControllerIT extends IntegrationTestBase {

    @Test
    void purchaseGame_unauthenticated_returns401() {
        assertThatThrownBy(() -> restClient.post()
                .uri("/API/games/1/purchase")
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void refundPurchase_unauthenticated_returns401() {
        assertThatThrownBy(() -> restClient.post()
                .uri("/API/purchases/1/refund")
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void getUserPurchases_unauthenticated_returns401() {
        assertThatThrownBy(() -> restClient.get()
                .uri("/API/users/1/purchases")
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }
}
