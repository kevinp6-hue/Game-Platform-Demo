package Kevin.Peyton.Game.Platform.Demo.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_genres")
public class GameGenre {
	@EmbeddedId
	private GameGenreId id;

	@ManyToOne
	@MapsId("gameId")
	@JoinColumn(name = "game_id")
	private Game game;

	@ManyToOne
	@MapsId("genreId")
	@JoinColumn(name = "genre_id")
	private Genre genre;

	public GameGenreId getId() {
		return id;
	}

	public void setId(GameGenreId id) {
		this.id = id;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

}
