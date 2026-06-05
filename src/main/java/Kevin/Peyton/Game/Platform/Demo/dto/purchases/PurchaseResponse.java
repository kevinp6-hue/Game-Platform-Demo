package Kevin.Peyton.Game.Platform.Demo.dto.purchases;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import Kevin.Peyton.Game.Platform.Demo.entity.Purchase;

public record PurchaseResponse(
    Integer id,
    Integer userId,
    Integer gameId,
    BigDecimal pricePaid,
    OffsetDateTime purchaseDate,
    Boolean isRefunded,
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