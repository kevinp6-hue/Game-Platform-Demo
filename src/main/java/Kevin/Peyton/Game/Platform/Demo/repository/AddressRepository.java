package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import Kevin.Peyton.Game.Platform.Demo.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    
}
