package Kevin.Peyton.Game.Platform.Demo.entity;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.OffsetDateTime;

@Entity
@Table(name = "library")
public class Library {
    @EmbeddedId
    private LibraryId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("gameId")
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "acquisition_date", insertable = false, updatable = false)
    private OffsetDateTime acquisitionDate;

    @Column(name = "total_playtime_minutes")
    private Integer totalPlaytimeMinutes;

    @Column(name = "last_played", insertable = false, updatable = false)
    private OffsetDateTime lastPlayed;

    @Column(name = "is_installed")
    private Boolean isInstalled;


    public LibraryId getId() {
        return id;
    }

    public void setId(LibraryId id) {
        this.id = id;
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

    public OffsetDateTime getAcquisitionDate() {
        return acquisitionDate;
    }

    public Integer getTotalPlaytimeMinutes() {
        return totalPlaytimeMinutes;
    }

    public void setTotalPlaytimeMinutes(Integer totalPlaytimeMinutes) {
        this.totalPlaytimeMinutes = totalPlaytimeMinutes;
    }

    public OffsetDateTime getLastPlayed() {
        return lastPlayed;
    }

    public Boolean getIsInstalled() {
        return isInstalled;
    }

    public void setIsInstalled(Boolean isInstalled) {
        this.isInstalled = isInstalled;
    }


}
