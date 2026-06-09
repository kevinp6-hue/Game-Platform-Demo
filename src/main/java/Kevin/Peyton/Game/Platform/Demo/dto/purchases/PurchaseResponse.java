package Kevin.Peyton.Game.Platform.Demo.dto.purchases;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import Kevin.Peyton.Game.Platform.Demo.entity.Purchase;

@Schema(description = "Record of a completed game purchase")
public record PurchaseResponse(
    @Schema(description = "Unique purchase ID", example = "101")
    Integer id,

    @Schema(description = "ID of the purchasing user", example = "42")
    Integer userId,

    @Schema(description = "ID of the purchased game", example = "1")
    Integer gameId,

    @Schema(description = "Price paid at time of purchase", example = "14.99")
    BigDecimal pricePaid,

    @Schema(description = "Timestamp when the purchase was made")
    OffsetDateTime purchaseDate,

    @Schema(description = "Whether the purchase has been refunded", example = "false")
    Boolean isRefunded,

    @Schema(description = "Timestamp when the refund was processed, null if not refunded")
    OffsetDateTime refundedAt
) {
    public static PurchaseResponse fromEntity(Purchase purchase) {
        return new PurchaseResponse(
            purchase.getId(),
            purchase.getUser().getId(),
            purchase.getGame().getId(),
            purchase.getPricePaid(),
            purchase.getPurchaseDate(),
            purchase.getIsRefunded(),
            purchase.getRefundedAt()
        );
    }
}
