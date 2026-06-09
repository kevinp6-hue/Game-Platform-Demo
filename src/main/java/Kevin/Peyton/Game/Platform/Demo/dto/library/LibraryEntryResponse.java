package Kevin.Peyton.Game.Platform.Demo.dto.library;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import Kevin.Peyton.Game.Platform.Demo.entity.Library;

@Schema(description = "A game in the user's personal library")
public record LibraryEntryResponse(
    @Schema(description = "User ID", example = "42")
    Integer userId,

    @Schema(description = "Game ID", example = "1")
    Integer gameId,

    @Schema(description = "Timestamp when the game was added to the library")
    OffsetDateTime acquisitionDate,

    @Schema(description = "Total minutes played", example = "320")
    Integer totalPlaytimeMinutes,

    @Schema(description = "Timestamp of the last play session")
    OffsetDateTime lastPlayed,

    @Schema(description = "Whether the game is currently installed", example = "true")
    Boolean isInstalled
) {
    public static LibraryEntryResponse fromEntity(Library library) {
        return new LibraryEntryResponse(
            library.getId().getUserId(),
            library.getId().getGameId(),
            library.getAcquisitionDate(),
            library.getTotalPlaytimeMinutes(),
            library.getLastPlayed(),
            library.getIsInstalled()
        );
    }
}
