package Kevin.Peyton.Game.Platform.Demo.dto.games;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record GameCreateRequest(
		@NotBlank String title,
		LocalDate releaseDate,
		@PositiveOrZero BigDecimal currentPrice,
		@NotNull Integer developerId
) {}
