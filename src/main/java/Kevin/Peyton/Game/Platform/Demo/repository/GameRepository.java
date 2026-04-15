package Kevin.Peyton.Game.Platform.Demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Kevin.Peyton.Game.Platform.Demo.entity.Game;

public interface GameRepository extends JpaRepository<Game, Integer> { 
    @Query("select g from Game g where g.developer.id = :developerId")
    List<Game> findByDeveloperId(@Param("developerId") Integer developerId);

    @Query("select g from Game g where g.title = :title")
    java.util.Optional<Game> findByTitle(@Param("title") String title);
}

