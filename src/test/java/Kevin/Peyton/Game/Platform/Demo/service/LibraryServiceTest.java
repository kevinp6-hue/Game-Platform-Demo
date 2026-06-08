package Kevin.Peyton.Game.Platform.Demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import Kevin.Peyton.Game.Platform.Demo.dto.library.LibraryUpdateRequest;
import Kevin.Peyton.Game.Platform.Demo.entity.Game;
import Kevin.Peyton.Game.Platform.Demo.entity.Library;
import Kevin.Peyton.Game.Platform.Demo.entity.LibraryId;
import Kevin.Peyton.Game.Platform.Demo.entity.User;

@DataJpaTest
@Import(LibraryService.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:library_service_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
public class LibraryServiceTest {

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private EntityManager entityManager;

    private User persistUser(String username) {
        var user = new User();
        user.setUsername(username);
        user.setPasswordHash("test_hash");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Game persistGame(String title) {
        var game = new Game();
        game.setTitle(title);
        entityManager.persist(game);
        entityManager.flush();
        return game;
    }

    private Library persistLibraryEntry(User user, Game game) {
        var libraryId = new LibraryId();
        libraryId.setUserId(user.getId());
        libraryId.setGameId(game.getId());

        var library = new Library();
        library.setId(libraryId);
        library.setUser(user);
        library.setGame(game);
        library.setTotalPlaytimeMinutes(0);
        library.setIsInstalled(false);

        entityManager.persist(library);
        entityManager.flush();
        return library;
    }

    @Test
    void getLibraryByUserId_returnsAllEntriesForUser() {
        var user = persistUser("lib_user");
        var game1 = persistGame("Game 1");
        var game2 = persistGame("Game 2");
        persistLibraryEntry(user, game1);
        persistLibraryEntry(user, game2);

        var entries = libraryService.getLibraryByUserId(user.getId());

        assertThat(entries).hasSize(2);
        assertThat(entries).allMatch(e -> e.getId().getUserId().equals(user.getId()));
    }

    @Test
    void getLibraryByUserId_unknownUser_throwsNotFound() {
        assertThatThrownBy(() -> libraryService.getLibraryByUserId(9999))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateLibraryEntry_updatesPlaytime() {
        var user = persistUser("playtime_user");
        var game = persistGame("Played Game");
        persistLibraryEntry(user, game);
        entityManager.clear();

        var updated = libraryService.updateLibraryEntry(user.getId(), game.getId(), new LibraryUpdateRequest(120, null));

        assertThat(updated.getTotalPlaytimeMinutes()).isEqualTo(120);
        assertThat(updated.getIsInstalled()).isFalse();
    }

    @Test
    void updateLibraryEntry_updatesInstalledState() {
        var user = persistUser("install_user");
        var game = persistGame("Installed Game");
        persistLibraryEntry(user, game);
        entityManager.clear();

        var updated = libraryService.updateLibraryEntry(user.getId(), game.getId(), new LibraryUpdateRequest(null, true));

        assertThat(updated.getIsInstalled()).isTrue();
        assertThat(updated.getTotalPlaytimeMinutes()).isEqualTo(0);
    }

    @Test
    void updateLibraryEntry_nullFields_changesNothing() {
        var user = persistUser("nochange_user");
        var game = persistGame("Unchanged Game");
        persistLibraryEntry(user, game);
        entityManager.clear();

        var updated = libraryService.updateLibraryEntry(user.getId(), game.getId(), new LibraryUpdateRequest(null, null));

        assertThat(updated.getTotalPlaytimeMinutes()).isEqualTo(0);
        assertThat(updated.getIsInstalled()).isFalse();
    }

    @Test
    void updateLibraryEntry_unknownEntry_throwsNotFound() {
        assertThatThrownBy(() -> libraryService.updateLibraryEntry(9999, 9999, new LibraryUpdateRequest(null, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
