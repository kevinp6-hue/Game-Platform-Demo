package Kevin.Peyton.Game.Platform.Demo.dto.library;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields that can be updated on a library entry (all optional)")
public record LibraryUpdateRequest(
    @Schema(description = "New total playtime in minutes", example = "350")
    Integer totalPlaytimeMinutes,

    @Schema(description = "Whether the game is currently installed", example = "false")
    Boolean isInstalled
) {}
