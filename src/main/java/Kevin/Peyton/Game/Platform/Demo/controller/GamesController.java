package Kevin.Peyton.Game.Platform.Demo.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Kevin.Peyton.Game.Platform.Demo.dto.games.GameCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.games.GameResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;
import Kevin.Peyton.Game.Platform.Demo.service.GamesService;


@Tag(name = "Games", description = "Browse the game catalog and manage game metadata")
@Validated
@RestController
@RequestMapping("/API/games")
public class GamesController {

    private final GamesService gamesService;

    public GamesController(GamesService gamesService) {
        this.gamesService = gamesService;
    }

    @Operation(summary = "List all games")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GameResponse.class))))
    @GetMapping
    public List<GameResponse> list() {
        var games = gamesService.getAllGames();
        return games.stream().map(GameResponse::fromEntity).toList();
    }

    @Operation(summary = "Get game by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "404", description = "Game not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public GameResponse getById(@Parameter(description = "Game ID") @PathVariable @Positive Integer id) {
        return GameResponse.fromEntity(gamesService.getGameById(id));
    }

    @Operation(summary = "Get game by title")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "404", description = "Game not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/name/{name}")
    public GameResponse getByName(@Parameter(description = "Game title") @PathVariable String name) {
        return GameResponse.fromEntity(gamesService.getGameByName(name));
    }

    @Operation(summary = "List games by developer ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GameResponse.class))))
    @GetMapping("/?developerId={developerId}")
    public List<GameResponse> getByDeveloperId(
            @Parameter(description = "Developer ID") @RequestParam @Positive Integer developerId) {
        var games = gamesService.getGamesByDeveloperId(developerId);
        return games.stream().map(GameResponse::fromEntity).toList();
    }

    @Operation(summary = "Get game by developer name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "404", description = "Developer not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/?developerName={developerName}")
    public GameResponse getByDeveloperName(
            @Parameter(description = "Developer name") @RequestParam String developerName) {
        return GameResponse.fromEntity(gamesService.getGameByDeveloperName(developerName));
    }

    @Operation(summary = "Create a new game", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Game created",
            content = @Content(schema = @Schema(implementation = GameResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Developer not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameCreateRequest request) {
        var created = gamesService.createGame(request);
        var body = GameResponse.fromEntity(created);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "Set genres for a game", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Genres updated"),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Game or genre not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}/genres")
    public ResponseEntity<Void> addGenres(
            @Parameter(description = "Game ID") @PathVariable @Positive @NotNull Integer id,
            @Valid @RequestBody @NotEmpty List<@NotNull @Positive Integer> genreIds) {
        genreIds.forEach(genreId -> gamesService.addGenreToGame(id, genreId));
        return ResponseEntity.noContent().build();
    }
}
