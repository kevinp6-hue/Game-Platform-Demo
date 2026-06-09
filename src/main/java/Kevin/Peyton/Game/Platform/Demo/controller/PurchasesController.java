package Kevin.Peyton.Game.Platform.Demo.controller;

import java.net.URI;
import java.util.List;
import java.security.Principal;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import Kevin.Peyton.Game.Platform.Demo.service.PurchasesService;
import Kevin.Peyton.Game.Platform.Demo.dto.purchases.PurchaseResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;

@Tag(name = "Purchases", description = "Buy games and request refunds")
@Validated
@RestController
public class PurchasesController {

    private final PurchasesService purchasesService;
    private final UserRepository userRepository;

    public PurchasesController(PurchasesService purchasesService, UserRepository userRepository) {
        this.purchasesService = purchasesService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Purchase a game", description = "Purchases the specified game for the authenticated user.",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Purchase successful",
            content = @Content(schema = @Schema(implementation = PurchaseResponse.class))),
        @ApiResponse(responseCode = "404", description = "Game not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Game already owned",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/API/games/{gameId}/purchase")
    public ResponseEntity<PurchaseResponse> purchaseGame(
            @Parameter(description = "Game ID") @PathVariable @Positive Integer gameId,
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

    @Operation(summary = "Refund a purchase", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Refund processed",
            content = @Content(schema = @Schema(implementation = PurchaseResponse.class))),
        @ApiResponse(responseCode = "404", description = "Purchase not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/API/purchases/{purchaseId}/refund")
    public ResponseEntity<PurchaseResponse> refundPurchase(
            @Parameter(description = "Purchase ID") @PathVariable @Positive Integer purchaseId) {
        var purchase = purchasesService.refundPurchase(purchaseId);
        return ResponseEntity.ok(PurchaseResponse.fromEntity(purchase));
    }

    @Operation(summary = "List purchases for a user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PurchaseResponse.class)))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/API/users/{userId}/purchases")
    public ResponseEntity<List<PurchaseResponse>> getUserPurchases(
            @Parameter(description = "User ID") @PathVariable @Positive Integer userId) {
        var purchases = purchasesService.getPurchasesByUserId(userId);
        return ResponseEntity.ok(purchases.stream().map(PurchaseResponse::fromEntity).toList());
    }
}
