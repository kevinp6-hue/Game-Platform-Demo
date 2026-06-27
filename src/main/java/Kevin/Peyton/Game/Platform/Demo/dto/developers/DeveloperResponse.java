package Kevin.Peyton.Game.Platform.Demo.dto.developers;


import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import Kevin.Peyton.Game.Platform.Demo.entity.Game;

import Kevin.Peyton.Game.Platform.Demo.entity.Developer;

import Kevin.Peyton.Game.Platform.Demo.dto.games.GameResponse;

public record DeveloperResponse(
    @Schema(description = "Unique developer ID", example = "1")
    Integer id,
    @Schema(description = "Developer's name", example = "John Doe")
    String devName,
    @Schema(description = "Developer's country", example = "United States")
    String country,
    @Schema(description = "Developer's bio", example = "John Doe is a talented game developer with over 10 years of experience.")
    String bio,
    @Schema(description = "List of games developed", example = "[{\"id\":1,\"title\":\"Hollow Knight\",\"releaseDate\":\"2017-02-24\",\"currentPrice\":14.99,\"salePrice\":9.99,\"description\":\"An epic action-adventure game set in a beautifully hand-drawn world.\",\"developerId\":7}]")
    List<GameResponse> games
) {
    public static DeveloperResponse fromEntity(Developer developer, List<Game> games) {
        List<GameResponse> gameResponses = games.stream()
                .map(GameResponse::fromEntity)
                .toList();
        return new DeveloperResponse(
                developer.getId(),
                developer.getDevName(),
                developer.getCountry(),
                developer.getBio(),
                gameResponses);
    }

}
