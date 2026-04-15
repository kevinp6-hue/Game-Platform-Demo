package Kevin.Peyton.Game.Platform.Demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import Kevin.Peyton.Game.Platform.Demo.dto.games.GameCreateRequest;
import Kevin.Peyton.Game.Platform.Demo.repository.DeveloperRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.GameGenreRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.GameRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.GenreRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.Developer;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;
import Kevin.Peyton.Game.Platform.Demo.entity.GameGenre;
import Kevin.Peyton.Game.Platform.Demo.entity.GameGenreId;
import Kevin.Peyton.Game.Platform.Demo.entity.Genre;

@Service
public class GamesService {
    private final GameRepository gameRepository;
    private final DeveloperRepository developerRepository;
    private final GenreRepository genreRepository;
    private final GameGenreRepository gameGenreRepository;

    public GamesService(
            GameRepository gameRepository,
            DeveloperRepository developerRepository,
            GenreRepository genreRepository,
            GameGenreRepository gameGenreRepository) {
        this.gameRepository = gameRepository;
        this.developerRepository = developerRepository;
        this.genreRepository = genreRepository;
        this.gameGenreRepository = gameGenreRepository;
    }

    @Transactional(readOnly = true)
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Game getGameById(Integer id) {
        return requireGame(id);
    }

    @Transactional(readOnly = true)
    public List<Game> getGamesByDeveloperId(Integer developerId) {
        return gameRepository.findByDeveloperId(developerId);
    }

    @Transactional(readOnly = true)
    public Game getGameByName(String name) {
        return gameRepository.findByTitle(name).orElseThrow(() -> new EntityNotFoundException("Game not found: " + name));
    }

    @Transactional
    public Game getGameByDeveloperName(String developerName) {
        var developer = developerRepository.findByName(developerName)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found: " + developerName));
        return gameRepository.findByDeveloperId(developer.getId()).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No games found for developer: " + developerName));
    }


    @Transactional
    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    @Transactional
    public Game createGame(GameCreateRequest request) {
        var game = new Game();
        game.setTitle(request.title());
        game.setReleaseDate(request.releaseDate());
        game.setCurrentPrice(request.currentPrice());

        if (request.developerId() != null) {
            game.setDeveloper(requireDeveloper(request.developerId()));
        }

        return gameRepository.save(game);
    }

    @Transactional
    public Game updateGame(Integer id, Game updatedGame) {
        var existingGame = requireGame(id);
        existingGame.setTitle(updatedGame.getTitle());
        existingGame.setReleaseDate(updatedGame.getReleaseDate());
        existingGame.setCurrentPrice(updatedGame.getCurrentPrice());
        existingGame.setDeveloper(updatedGame.getDeveloper());
        return existingGame;
    }

    @Transactional
    public void addGenreToGame(Integer gameId, Integer genreId) {
        var game = requireGame(gameId);
        var genre = requireGenre(genreId);
        var id = new GameGenreId(game.getId(), genre.getId());

        if (gameGenreRepository.existsById(id)) {
            return;
        }

        var gameGenre = new GameGenre();
        gameGenre.setId(id);
        gameGenre.setGame(game);
        gameGenre.setGenre(genre);

        gameGenreRepository.save(gameGenre);
    }

    private Game requireGame(Integer id) {
        return gameRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Game not found: " + id));
    }

    private Genre requireGenre(Integer id) {
        return genreRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Genre not found: " + id));
    }

    private Developer requireDeveloper(Integer id) {
        return developerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found: " + id));
    }
}
