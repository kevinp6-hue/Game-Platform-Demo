package Kevin.Peyton.Game.Platform.Demo.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.catalina.connector.Response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import Kevin.Peyton.Game.Platform.Demo.GamePlatformDemoApplication;
import Kevin.Peyton.Game.Platform.Demo.support.IntegrationTestBase;

import Kevin.Peyton.Game.Platform.Demo.dto.users.UserCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.users.UserResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.auth.LoginRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.auth.AuthResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.purchases.PurchaseResponse;


@ActiveProfiles("test")
@SpringBootTest(classes = GamePlatformDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class PurchasesControllerIT extends IntegrationTestBase {
    private String bearerToken;
    private Integer testUserId;

    @BeforeEach
    void setUp() {
        var user = restClient.post()
                .uri("/API/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserCreateRequest("testuser", null,"testpassword"))
                .retrieve()
                .body(UserResponse.class);
        testUserId = user.id();

        var auth = restClient.post()
                .uri("/API/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest("testuser", "testpassword"))
                .retrieve()
                .body(AuthResponse.class);

        bearerToken = "Bearer " + auth.token();

    }


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

   
    @Test
    void purchaseGame_authenticated_returns201() {
        var response = restClient.post()
                .uri("/API/games/1/purchase")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .toEntity(PurchaseResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isRefunded()).isFalse();
    }

    @Test
    void purchaseGame_duplicate_returns409() {
        // First purchase should succeed
        restClient.post()
                .uri("/API/games/2/purchase")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .toEntity(PurchaseResponse.class);

        // Second purchase of the same game should fail with 409 Conflict
        assertThatThrownBy(() -> restClient.post()
                .uri("/API/games/2/purchase")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.Conflict.class);
    }

    @Test
    void refundPurchase_notOwned_returns404() {
        assertThatThrownBy(() -> restClient.post()
                .uri("/API/purchases/999/refund") // Assuming 999 is a non-existent purchase ID
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .toBodilessEntity())
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void refundPurchase_authenticated_returns200() {
    var purchase = restClient.post()
            .uri("/API/games/3/purchase")
            .header(HttpHeaders.AUTHORIZATION, bearerToken)
            .retrieve()
            .body(PurchaseResponse.class);

    var response = restClient.post()
            .uri("/API/purchases/" + purchase.id() + "/refund")
            .header(HttpHeaders.AUTHORIZATION, bearerToken)
            .retrieve()
            .toEntity(PurchaseResponse.class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().isRefunded()).isTrue();
    }

    @Test
    void getUserPurchases_returnsListForUser() {
    restClient.post().uri("/API/games/4/purchase")
            .header(HttpHeaders.AUTHORIZATION, bearerToken).retrieve().toBodilessEntity();
    restClient.post().uri("/API/games/5/purchase")
            .header(HttpHeaders.AUTHORIZATION, bearerToken).retrieve().toBodilessEntity();

    var response = restClient.get()
            .uri("/API/users/" + testUserId + "/purchases")
            .header(HttpHeaders.AUTHORIZATION, bearerToken)
            .retrieve()
            .toEntity(PurchaseResponse[].class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).hasSize(2);
}

}
