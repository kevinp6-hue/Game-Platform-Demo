package Kevin.Peyton.Game.Platform.Demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import Kevin.Peyton.Game.Platform.Demo.entity.Game;
import Kevin.Peyton.Game.Platform.Demo.entity.User;
import Kevin.Peyton.Game.Platform.Demo.exception.ConflictException;

@DataJpaTest
@Import(PurchasesService.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:purchases_service_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
public class PurchasesServiceTest {

    @Autowired
    private PurchasesService purchasesService;

    @Autowired
    private EntityManager entityManager;

    private User persistUser(String username) {
        var user = new User();
        user.setUsername(username);
        user.setPasswordHash("test_hash");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Game persistGame(String title, BigDecimal price) {
        var game = new Game();
        game.setTitle(title);
        game.setCurrentPrice(price);
        entityManager.persist(game);
        entityManager.flush();
        return game;
    }

    @Test
    void purchaseGame_happyPath_createsPurchase() {
        var user = persistUser("buyer");
        var game = persistGame("Test Game", new BigDecimal("29.99"));

        var purchase = purchasesService.purchaseGame(user.getId(), game.getId());

        assertThat(purchase.getId()).isNotNull();
        assertThat(purchase.getUser().getId()).isEqualTo(user.getId());
        assertThat(purchase.getGame().getId()).isEqualTo(game.getId());
        assertThat(purchase.getPricePaid()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(purchase.getIsRefunded()).isFalse();
    }

    @Test
    void purchaseGame_duplicate_throwsConflict() {
        var user = persistUser("buyer2");
        var game = persistGame("Popular Game", new BigDecimal("19.99"));
        purchasesService.purchaseGame(user.getId(), game.getId());

        assertThatThrownBy(() -> purchasesService.purchaseGame(user.getId(), game.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void purchaseGame_afterRefund_succeeds() {
        var user = persistUser("buyer3");
        var game = persistGame("Refundable Game", new BigDecimal("9.99"));
        var purchase = purchasesService.purchaseGame(user.getId(), game.getId());
        purchasesService.refundPurchase(purchase.getId());

        var repurchase = purchasesService.purchaseGame(user.getId(), game.getId());

        assertThat(repurchase.getId()).isNotEqualTo(purchase.getId());
        assertThat(repurchase.getIsRefunded()).isFalse();
    }

    @Test
    void purchaseGame_unknownUser_throwsNotFound() {
        var game = persistGame("Some Game", new BigDecimal("14.99"));

        assertThatThrownBy(() -> purchasesService.purchaseGame(9999, game.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void purchaseGame_unknownGame_throwsNotFound() {
        var user = persistUser("buyer4");

        assertThatThrownBy(() -> purchasesService.purchaseGame(user.getId(), 9999))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void refundPurchase_happyPath_setsRefunded() {
        var user = persistUser("refunder");
        var game = persistGame("Refund Game", new BigDecimal("49.99"));
        var purchase = purchasesService.purchaseGame(user.getId(), game.getId());

        var refunded = purchasesService.refundPurchase(purchase.getId());

        assertThat(refunded.getIsRefunded()).isTrue();
    }

    @Test
    void refundPurchase_alreadyRefunded_throwsConflict() {
        var user = persistUser("refunder2");
        var game = persistGame("Already Refunded Game", new BigDecimal("39.99"));
        var purchase = purchasesService.purchaseGame(user.getId(), game.getId());
        purchasesService.refundPurchase(purchase.getId());

        assertThatThrownBy(() -> purchasesService.refundPurchase(purchase.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void refundPurchase_unknownPurchase_throwsNotFound() {
        assertThatThrownBy(() -> purchasesService.refundPurchase(9999))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPurchasesByUserId_returnsAllPurchasesForUser() {
        var user = persistUser("history_user");
        var game1 = persistGame("Game 1", new BigDecimal("9.99"));
        var game2 = persistGame("Game 2", new BigDecimal("19.99"));
        purchasesService.purchaseGame(user.getId(), game1.getId());
        purchasesService.purchaseGame(user.getId(), game2.getId());

        var purchases = purchasesService.getPurchasesByUserId(user.getId());

        assertThat(purchases).hasSize(2);
        assertThat(purchases).allMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    @Test
    void getPurchasesByUserId_unknownUser_throwsNotFound() {
        assertThatThrownBy(() -> purchasesService.getPurchasesByUserId(9999))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
