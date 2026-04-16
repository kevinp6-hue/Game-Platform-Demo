package Kevin.Peyton.Game.Platform.Demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Kevin.Peyton.Game.Platform.Demo.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}

