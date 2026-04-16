package Kevin.Peyton.Game.Platform.Demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Kevin.Peyton.Game.Platform.Demo.entity.User;


public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = {"emails", "addresses"})
    Optional<User> findWithEmailsAndAddressesById(Integer id);

    @EntityGraph(attributePaths = {"emails"})
    Optional<User> findWithEmailsById(Integer id);

    @Query("select distinct u from User u join fetch u.emails e where e.email = :email")
    Optional<User> findWithEmailsByEmail(String email);

    Optional<User> findByUsername(String username);
}
