package Kevin.Peyton.Game.Platform.Demo.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Integer id;

    @Column(name = "price_paid")
    private BigDecimal pricePaid;

    @Column(name = "purchase_date", insertable = false, updatable = false)
    private OffsetDateTime purchaseDate;

    @Column(name = "is_refunded")
    private Boolean isRefunded = false;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(BigDecimal pricePaid) {
        this.pricePaid = pricePaid;
    }

    public OffsetDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(OffsetDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Boolean getIsRefunded() {
        return isRefunded;
    }

    public void setIsRefunded(Boolean isRefunded) {
        this.isRefunded = isRefunded;
    }

    public OffsetDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(OffsetDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }


}