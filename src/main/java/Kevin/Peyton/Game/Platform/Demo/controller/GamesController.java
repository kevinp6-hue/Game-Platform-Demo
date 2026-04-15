package Kevin.Peyton.Game.Platform.Demo.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
import Kevin.Peyton.Game.Platform.Demo.service.GamesService;
import Kevin.Peyton.Game.Platform.Demo.dto.games.GameResponse;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;


@Validated
@RestController
@RequestMapping("/API/games")
public class GamesController {

    private final GamesService gamesService;

    public GamesController(GamesService gamesService) {
        this.gamesService = gamesService;
    }

    /**
     * Endpoint to retrieve a list of all games.
     * @return A list of all games.
     */
	@GetMapping
    public List<GameResponse> list() {
        var games = gamesService.getAllGames();
        return games.stream().map(GameResponse::fromEntity).toList();
    }
    
    /**
     * Endpoint to retrieve a game by its ID.
     * @param id The ID of the game to retrieve.
     * @return The game with the specified ID.
     */
    @GetMapping("/{id}")
    public GameResponse getById(@PathVariable @Positive Integer id) {

        return GameResponse.fromEntity(gamesService.getGameById(id));
    }

    /**
     * Endpoint to retrieve a game by its name.
     * @param name The name of the game to retrieve.
     * @return The game with the specified name.
     */
    @GetMapping("/name/{name}")
    public GameResponse getByName(@PathVariable String name) {
        return GameResponse.fromEntity(gamesService.getGameByName(name));
    }

    /**
     * Endpoint to retrieve games by developer ID.
     * @param developerId The ID of the developer whose games to retrieve.
     * @return A list of games by the specified developer.
     */
    @GetMapping("/?developerId={developerId}")
    public List<GameResponse> getByDeveloperId(@RequestParam @Positive Integer developerId) {
        var games = gamesService.getGamesByDeveloperId(developerId);
        return games.stream().map(GameResponse::fromEntity).toList();
    }

    /**
     * Endpoint to retrieve a game by developer name.
     * @param developerName The name of the developer whose game to retrieve.
     * @return The game by the specified developer.
     */

    @GetMapping("/?developerName={developerName}")
    public GameResponse getByDeveloperName(@RequestParam String developerName) {
        return GameResponse.fromEntity(gamesService.getGameByDeveloperName(developerName));
    }
    //////// POST ENDPOINTS

    /**
     * Endpoint to create a new game.
     * @param request The request containing the game details.
     * @return A response entity containing the created game and the location of the new resource.
     */
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

    /**
     * Endpoint to add multiple genres to a game.
     * @param id The ID of the game to which to add genres. Id must be positive integer.
     * @param genreIds A list of genre IDs to add.
     * @return The updated game with the added genres.
     */
    @PutMapping("/{id}/genres")
    public ResponseEntity<Void> addGenres(@PathVariable @Positive @NotNull Integer id, @Valid @RequestBody @NotEmpty List<@NotNull @Positive Integer> genreIds) {

        genreIds.forEach(genreId -> gamesService.addGenreToGame(id, genreId));
        return ResponseEntity.noContent().build();
    }

    
     
     

}

