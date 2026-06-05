package Kevin.Peyton.Game.Platform.Demo.repository;
import Kevin.Peyton.Game.Platform.Demo.entity.Purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    @Query("select p from Purchase p where p.user.id = :userId")
    List<Purchase> findByUser_Id(@Param("userId") Integer userId);

    @Query("select p from Purchase p where p.user.id = :userId and p.game.id = :gameId and p.isRefunded = :isRefunded")
    List<Purchase> findByUser_IdAndGame_IdAndIsRefunded(@Param("userId") Integer userId, @Param("gameId") Integer gameId, @Param("isRefunded") Boolean isRefunded);

}
