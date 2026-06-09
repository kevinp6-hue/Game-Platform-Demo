package Kevin.Peyton.Game.Platform.Demo.dto.games;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields required to add a new game to the catalog")
public record GameCreateRequest(
    @Schema(description = "Game title", example = "Hollow Knight")
    @NotBlank String title,

    @Schema(description = "Release date (ISO-8601)", example = "2017-02-24")
    LocalDate releaseDate,

    @Schema(description = "Current price in USD (0 for free-to-play)", example = "14.99")
    @PositiveOrZero BigDecimal currentPrice,

    @Schema(description = "ID of the developer who created this game", example = "7")
    @NotNull Integer developerId
) {}
