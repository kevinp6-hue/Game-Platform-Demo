package Kevin.Peyton.Game.Platform.Demo.controller;

import java.net.URI;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.security.Principal;

import Kevin.Peyton.Game.Platform.Demo.service.PurchasesService;
import Kevin.Peyton.Game.Platform.Demo.dto.purchases.PurchaseResponse;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;

@Validated
@RestController
public class PurchasesController {
    
    private final PurchasesService purchasesService;

    private final UserRepository userRepository;

    public PurchasesController(PurchasesService purchasesService, UserRepository userRepository) {
        this.purchasesService = purchasesService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint to purchase a game for a user.
     * @param gameId The ID of the game to purchase.
     * @param principal The authenticated user's principal.
     */
    @PostMapping("/API/games/{gameId}/purchase")
    public ResponseEntity<PurchaseResponse> purchaseGame(
            @PathVariable @Positive Integer gameId,
            Principal principal) {
        var userId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + principal.getName()))
                .getId();
        var purchase = purchasesService.purchaseGame(userId, gameId);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(purchase.getId())
                .toUri();
        return ResponseEntity.created(location).body(PurchaseResponse.fromEntity(purchase));
    }

    /**
     * Endpoint to refund a purchase.
     * @param purchaseId
     * @return
     */
    @PostMapping("/API/purchases/{purchaseId}/refund")
    public ResponseEntity<PurchaseResponse> refundPurchase(
            @PathVariable @Positive Integer purchaseId) {

        var purchase = purchasesService.refundPurchase(purchaseId);
        return ResponseEntity.ok(PurchaseResponse.fromEntity(purchase));
    }

    @GetMapping("/API/users/{userId}/purchases")
    public ResponseEntity<List<PurchaseResponse>> getUserPurchases(
            @PathVariable @Positive Integer userId) {
        var purchases = purchasesService.getPurchasesByUserId(userId);
        return ResponseEntity.ok(purchases.stream().map(PurchaseResponse::fromEntity).toList());
    }
}
