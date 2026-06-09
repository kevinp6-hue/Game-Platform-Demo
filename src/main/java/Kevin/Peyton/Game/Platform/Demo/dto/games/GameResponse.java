package Kevin.Peyton.Game.Platform.Demo.dto.games;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

import Kevin.Peyton.Game.Platform.Demo.entity.Game;

@Schema(description = "Game catalog entry")
public record GameResponse(
    @Schema(description = "Unique game ID", example = "1")
    Integer id,

    @Schema(description = "Game title", example = "Hollow Knight")
    String title,

    @Schema(description = "Release date", example = "2017-02-24")
    LocalDate releaseDate,

    @Schema(description = "Current price in USD", example = "14.99")
    BigDecimal currentPrice,

    @Schema(description = "ID of the developer", example = "7")
    Integer developerId
) {
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
