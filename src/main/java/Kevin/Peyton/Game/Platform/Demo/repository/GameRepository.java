package Kevin.Peyton.Game.Platform.Demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import Kevin.Peyton.Game.Platform.Demo.entity.Game;

public interface GameRepository extends JpaRepository<Game, Integer> { 
    @Query("select g from Game g where g.developer.id = :developerId")
    List<Game> findByDeveloperId(@Param("developerId") Integer developerId);

    @Query("select g from Game g where g.title = :title")
    java.util.Optional<Game> findByTitle(@Param("title") String title);

    @Query("SELECT DISTINCT g FROM Game g WHERE " +
       "(:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
       "(:genreId IS NULL OR EXISTS (SELECT gg FROM GameGenre gg WHERE gg.id.gameId = g.id AND gg.id.genreId = :genreId))")
    Page<Game> search(@Param("title") String title, @Param("genreId") Integer genreId, Pageable pageable);


}

