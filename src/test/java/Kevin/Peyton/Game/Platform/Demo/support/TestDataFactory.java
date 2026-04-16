package Kevin.Peyton.Game.Platform.Demo.support;

import java.math.BigDecimal;
import java.time.LocalDate;

import Kevin.Peyton.Game.Platform.Demo.dto.games.GameCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.entity.Developer;

/**
 * Central place for common test fixtures. Keep defaults realistic, and override
 * fields per-test when needed.
 */
public final class TestDataFactory {
	private TestDataFactory() {
	}

	public static Developer developer() {
		var developer = new Developer();
		developer.setDevName("Test Developer");
		developer.setCountry("USA");
		return developer;
	}

	public static GameCreateRequest gameCreateRequest(Integer developerId) {
		return new GameCreateRequest(
				"Test Game",
				LocalDate.of(2024, 1, 1),
				BigDecimal.valueOf(59.99),
				developerId);
	}
}

