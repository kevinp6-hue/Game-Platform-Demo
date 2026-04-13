package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.UserEmail;

public interface UserEmailRepository extends JpaRepository<UserEmail, Integer> {
    
}
