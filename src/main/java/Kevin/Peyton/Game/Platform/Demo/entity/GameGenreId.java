package Kevin.Peyton.Game.Platform.Demo.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GameGenreId implements Serializable {
	@Column(name = "game_id")
	private Integer gameId;

	@Column(name = "genre_id")
	private Integer genreId;

	public GameGenreId() {
	}

	public GameGenreId(Integer gameId, Integer genreId) {
		this.gameId = gameId;
		this.genreId = genreId;
	}

	public Integer getGameId() {
		return gameId;
	}

	public Integer getGenreId() {
		return genreId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GameGenreId otherId)) {
			return false;
		}
		return Objects.equals(gameId, otherId.gameId) && Objects.equals(genreId, otherId.genreId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameId, genreId);
	}
}
