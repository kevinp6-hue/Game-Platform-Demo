package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Kevin.Peyton.Game.Platform.Demo.entity.Developer;

public interface DeveloperRepository extends JpaRepository<Developer, Integer> {
    @Query("select d from Developer d where d.name = :name")
    java.util.Optional<Developer> findByName(String name);
}
