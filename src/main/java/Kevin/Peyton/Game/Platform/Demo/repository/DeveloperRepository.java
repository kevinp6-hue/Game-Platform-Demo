package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Kevin.Peyton.Game.Platform.Demo.entity.Developer;

public interface DeveloperRepository extends JpaRepository<Developer, Integer> {
    java.util.Optional<Developer> findByDevName(String devName);
}
