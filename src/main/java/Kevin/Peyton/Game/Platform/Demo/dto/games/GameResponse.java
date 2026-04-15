package Kevin.Peyton.Game.Platform.Demo.dto.games;

import java.math.BigDecimal;
import java.time.LocalDate;

import Kevin.Peyton.Game.Platform.Demo.entity.Game;

public record GameResponse(
		Integer id,
		String title,
		LocalDate releaseDate,
		BigDecimal currentPrice,
		Integer developerId) {

	public static GameResponse fromEntity(Game game) {
		var developer = game.getDeveloper();
		return new GameResponse(
				game.getId(),
				game.getTitle(),
				game.getReleaseDate(),
				game.getCurrentPrice(),
				developer == null ? null : developer.getId());
	}

	
}
