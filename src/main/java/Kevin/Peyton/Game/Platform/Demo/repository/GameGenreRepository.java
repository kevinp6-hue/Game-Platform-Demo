package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.GameGenre;
import Kevin.Peyton.Game.Platform.Demo.entity.GameGenreId;

public interface GameGenreRepository extends JpaRepository<GameGenre, GameGenreId> {
    
}
