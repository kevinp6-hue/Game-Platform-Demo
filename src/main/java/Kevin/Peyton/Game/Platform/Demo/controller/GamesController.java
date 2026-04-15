package Kevin.Peyton.Game.Platform.Demo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import Kevin.Peyton.Game.Platform.Demo.dto.games.GameCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.service.GamesService;
import Kevin.Peyton.Game.Platform.Demo.dto.games.GameResponse;


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
    public GameResponse getById(@PathVariable Integer id) {

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
    public List<GameResponse> getByDeveloperId(@PathVariable Integer developerId) {
        var games = gamesService.getGamesByDeveloperId(developerId);
        return games.stream().map(GameResponse::fromEntity).toList();
    }

    /**
     * Endpoint to retrieve a game by developer name.
     * @param developerName The name of the developer whose game to retrieve.
     * @return The game by the specified developer.
     */

    @GetMapping("/?developerName={developerName}")
    public GameResponse getByDeveloperName(@PathVariable String developerName) {
        return GameResponse.fromEntity(gamesService.getGameByDeveloperName(developerName));
    }
    //////// POST ENDPOINTS

    /**
     * Endpoint to create a new game.
     * @param request The request containing the game details.
     * @return The created game.
     */
    @PostMapping
    public GameResponse create(@Valid @RequestBody GameCreateRequest request) {
        return GameResponse.fromEntity(gamesService.createGame(request));
    }

    /**
     * Endpoint to add multiple genres to a game.
     * @param id The ID of the game to which to add genres. Id must be positive integer.
     * @param genreIds A list of genre IDs to add.
     * @return The updated game with the added genres.
     */
    @PostMapping("/{id}/genres")
    public GameResponse addGenres(@Valid @PathVariable Integer id, @RequestBody List<Integer> genreIds) {
        if (id <= 0) {
            throw new IllegalArgumentException("Game ID must be a positive integer.");
        }
        for (Integer genreId : genreIds) {
            gamesService.addGenreToGame(id, genreId);
        }
        return GameResponse.fromEntity(gamesService.getGameById(id));
    }

    /**
     * Endpoint to add a single genre to a game.
     * @param id The ID of the game to which to add the genre.
     * @param genreId The ID of the genre to add.
     * @return The updated game with the added genre.
     */
    @PostMapping("/{id}/genre")
    public GameResponse addGenre(@Valid @PathVariable Integer id, @RequestBody Integer genreId) {
        if (id <= 0) {
            throw new IllegalArgumentException("Game ID must be a positive integer.");
        }
        if (genreId <= 0) {
            throw new IllegalArgumentException("Genre ID must be a positive integer.");
        }
        gamesService.addGenreToGame(id, genreId);
        return GameResponse.fromEntity(gamesService.getGameById(id));
    }
    
     
     

}

