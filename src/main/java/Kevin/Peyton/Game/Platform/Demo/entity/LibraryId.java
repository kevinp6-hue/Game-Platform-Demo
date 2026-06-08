package Kevin.Peyton.Game.Platform.Demo.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LibraryId implements Serializable {
    
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "game_id")
    private Integer gameId;


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    @Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LibraryId otherId)) {
			return false;
		}
		return Objects.equals(userId, otherId.userId) && Objects.equals(gameId, otherId.gameId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, gameId);
	}
}
