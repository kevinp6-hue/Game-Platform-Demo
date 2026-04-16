package Kevin.Peyton.Game.Platform.Demo.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import Kevin.Peyton.Game.Platform.Demo.entity.Developer;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;
import Kevin.Peyton.Game.Platform.Demo.repository.GameRepository;

@DataJpaTest
@TestPropertySource(properties = {
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
  "spring.datasource.driverClassName=org.h2.Driver",
  "spring.datasource.username=sa",
  "spring.datasource.password="
})
public class GameRepositoryTest {
 
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    private static Developer developer(String devName) {
        Developer developer = new Developer();
        developer.setDevName(devName);
        developer.setCountry("USA");
        return developer;
    }

    private static Game game(String title, Developer developer) {
        Game game = new Game();
        game.setTitle(title);
        game.setCurrentPrice(new BigDecimal("23.45"));
        game.setReleaseDate(LocalDate.of(2020, 1, 2));
        game.setDeveloper(developer);
        return game;
    }

    /**
     * Test saving and finding a game in the repository.
     */
    @Test
    void testSaveAndFindGame() {
        Developer developer = developer("Test Dev");
        entityManager.persist(developer);

        Game savedGame = gameRepository.saveAndFlush(
                game("Test Game", developer)
        );

        assertAll(
                () -> assertNotNull(savedGame.getId()),
                () -> assertEquals("Test Game", savedGame.getTitle()),
                () -> assertEquals(new BigDecimal("23.45"), savedGame.getCurrentPrice()),
                () -> assertEquals(LocalDate.of(2020, 1, 2), savedGame.getReleaseDate()),
                () -> assertNotNull(savedGame.getDeveloper()),
                () -> assertEquals(developer.getId(), savedGame.getDeveloper().getId())
        );

        entityManager.clear();

        Game foundGame = gameRepository.findById(savedGame.getId()).orElseThrow();
        assertEquals(savedGame.getId(), foundGame.getId());
    }

    @Test
    void testFindByTitle() {
        Developer developer = developer("FindByTitle Dev");
        entityManager.persist(developer);

        gameRepository.saveAndFlush(game("Unique Title", developer));
        entityManager.clear();

        Game found = gameRepository.findByTitle("Unique Title").orElseThrow();
        assertEquals("Unique Title", found.getTitle());
    }

    @Test
    void testFindByTitleReturnsEmptyWhenMissing() {
        assertTrue(gameRepository.findByTitle("Does Not Exist").isEmpty());
    }

    @Test
    void testFindByDeveloperId() {
        Developer devA = developer("Dev A");
        Developer devB = developer("Dev B");
        entityManager.persist(devA);
        entityManager.persist(devB);

        gameRepository.save(game("Game A1", devA));
        gameRepository.save(game("Game A2", devA));
        gameRepository.saveAndFlush(game("Game B1", devB));
        entityManager.clear();

        List<Game> found = gameRepository.findByDeveloperId(devA.getId());
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(g -> g.getDeveloper() != null && devA.getId().equals(g.getDeveloper().getId())));
    }

    @Test
    void testSaveFailsWhenTitleIsNull() {
        Developer developer = developer("NullTitle Dev");
        entityManager.persist(developer);

        Game game = game(null, developer);
        assertThrows(DataIntegrityViolationException.class, () -> gameRepository.saveAndFlush(game));
    }

}
