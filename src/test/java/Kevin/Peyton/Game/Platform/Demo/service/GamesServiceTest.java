package Kevin.Peyton.Game.Platform.Demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

import Kevin.Peyton.Game.Platform.Demo.service.GamesService;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;
import Kevin.Peyton.Game.Platform.Demo.entity.Developer;

@DataJpaTest
@Import(GamesService.class)
@TestPropertySource(properties = {
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
  "spring.datasource.driverClassName=org.h2.Driver",
  "spring.datasource.username=sa",
  "spring.datasource.password="
})
public class GamesServiceTest {
    
    @Autowired
    private GamesService gamesService;

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
     * Test that getting all games returns an empty list when there are no games in the database.
     */
     @Test
     void testGetAllGamesEmpty() {
         List<Game> games = gamesService.getAllGames();
         assertEquals(0, games.size());
     }

     /**
      * Test that getting all games returns the games that were saved in the database.
      */
     @Test 
     void testGetAllGamesReturnsSavedGames() {
         Developer developer = developer("Test Dev");
         entityManager.persist(developer);

         Game game1 = game("Test Game 1", developer);
         Game game2 = game("Test Game 2", developer);
         entityManager.persist(game1);
         entityManager.persist(game2);
         entityManager.flush();

         List<Game> games = gamesService.getAllGames();
         assertEquals(2, games.size());
     }

     /**
      * Test that getting games by developer ID returns only the games associated with that developer.
      */
     @Test
     void testGetGamesByDeveloperId() {
         Developer developer1 = developer("Dev 1");
         Developer developer2 = developer("Dev 2");
         entityManager.persist(developer1);
         entityManager.persist(developer2);

         Game game1 = game("Game 1", developer1);
         Game game2 = game("Game 2", developer1);
         Game game3 = game("Game 3", developer2);
         entityManager.persist(game1);
         entityManager.persist(game2);
         entityManager.persist(game3);
         entityManager.flush();

         List<Game> dev1Games = gamesService.getGamesByDeveloperId(developer1.getId());
         List<Game> dev2Games = gamesService.getGamesByDeveloperId(developer2.getId());

         assertEquals(2, dev1Games.size());
         assertEquals(1, dev2Games.size());
     }

    /**
     * Test that getting games by developer ID returns an empty list when there are no games for that developer.
     */
    @Test
    void testGetGamesByDeveloperIdReturnsEmptyWhenNoGames() {
        Developer developer = developer("Lonely Dev");
        entityManager.persist(developer);
        entityManager.flush();
    
        List<Game> games = gamesService.getGamesByDeveloperId(developer.getId());
        assertEquals(0, games.size());
    }

    /**
    * Test that getting games by developer ID returns an empty list when the developer does not exist.
    */
    @Test
    void testGetGamesByDeveloperIdReturnsEmptyWhenDeveloperMissing() {
        List<Game> games = gamesService.getGamesByDeveloperId(9999);
        assertEquals(0, games.size());
    }
}
