package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    
}
