package Kevin.Peyton.Game.Platform.Demo.dto.library;

import Kevin.Peyton.Game.Platform.Demo.entity.Library;

import java.time.OffsetDateTime;

public record LibraryEntryResponse(
    Integer userId,
    Integer gameId,
    OffsetDateTime acquisitionDate,
    Integer totalPlaytimeMinutes,
    OffsetDateTime lastPlayed,
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
