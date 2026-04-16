package Kevin.Peyton.Game.Platform.Demo.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

import Kevin.Peyton.Game.Platform.Demo.entity.Developer;
import Kevin.Peyton.Game.Platform.Demo.repository.DeveloperRepository;

@DataJpaTest
@TestPropertySource(properties = {
  "spring.flyway.enabled=false",
  "spring.jpa.hibernate.ddl-auto=create-drop",
  "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
  "spring.datasource.driverClassName=org.h2.Driver",
  "spring.datasource.username=sa",
  "spring.datasource.password="
})
public class DeveloperRepositoryTest {

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private EntityManager entityManager;

    private static Developer developer(String devName, String country, Integer ownerUserId) {
        Developer developer = new Developer();
        developer.setDevName(devName);
        developer.setCountry(country);
        developer.setOwnerUserId(ownerUserId);
        return developer;
    }

    /**
     * Test saving and finding a developer in the repository.
     */
    @Test
    void testSaveAndFindDeveloper() {
        Developer savedDeveloper = developerRepository.saveAndFlush(
                developer("Test Developer", "USA", 123)
        );

        assertAll(
                () -> assertNotNull(savedDeveloper.getId()),
                () -> assertEquals("Test Developer", savedDeveloper.getDevName()),
                () -> assertEquals("USA", savedDeveloper.getCountry()),
                () -> assertEquals(123, savedDeveloper.getOwnerUserId())
        );

        entityManager.clear();

        Developer foundDeveloper = developerRepository.findById(savedDeveloper.getId()).orElseThrow();
        assertEquals(savedDeveloper.getId(), foundDeveloper.getId());
    }

    /**
     * Test finding a developer by name in the repository.
     */
    @Test
    void testFindByDevName() {
        developerRepository.saveAndFlush(developer("Unique Dev Name", "UK", null));
        entityManager.clear();

        Developer foundDeveloper = developerRepository.findByDevName("Unique Dev Name").orElseThrow();
        assertEquals("Unique Dev Name", foundDeveloper.getDevName());
    }

    /**
     * Test finding a developer by name returns empty when not found.
     */
    @Test
    void testFindByDevNameReturnsEmptyWhenMissing() {
        assertTrue(developerRepository.findByDevName("Does Not Exist").isEmpty());
    }

    /**
     * Test that saving a developer with a null name violates the not-null constraint.
     */
    @Test
    void testSaveFailsWhenDevNameIsNull() {
        Developer developer = new Developer();
        developer.setCountry("USA");

        assertThrows(DataIntegrityViolationException.class, () -> developerRepository.saveAndFlush(developer));
    }

    /**
     *  Test that updating a developer's country persists the changes in the database.
     */
    @Test
    void testUpdateDeveloperPersistsChanges() {
        Developer savedDeveloper = developerRepository.saveAndFlush(developer("Updatable Dev", "USA", null));
        entityManager.clear();

        Developer loadedDeveloper = developerRepository.findById(savedDeveloper.getId()).orElseThrow();
        loadedDeveloper.setCountry("Canada");
        developerRepository.saveAndFlush(loadedDeveloper);
        entityManager.clear();

        Developer reloadedDeveloper = developerRepository.findById(savedDeveloper.getId()).orElseThrow();
        assertEquals("Canada", reloadedDeveloper.getCountry());
    }

    @Test
    void testDeleteDeveloperRemovesRow() {
        Developer savedDeveloper = developerRepository.saveAndFlush(developer("Deletable Dev", "USA", null));

        developerRepository.deleteById(savedDeveloper.getId());
        developerRepository.flush();

        assertTrue(developerRepository.findById(savedDeveloper.getId()).isEmpty());
    }
}
