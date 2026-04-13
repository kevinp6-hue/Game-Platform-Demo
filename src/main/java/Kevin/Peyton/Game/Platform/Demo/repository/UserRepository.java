package Kevin.Peyton.Game.Platform.Demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.User;


public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = {"emails", "addresses"})
    Optional<User> findWithEmailsAndAddressesById(Integer id);

    @EntityGraph(attributePaths = {"emails"})
    Optional<User> findWithEmailsById(Integer id);
}
