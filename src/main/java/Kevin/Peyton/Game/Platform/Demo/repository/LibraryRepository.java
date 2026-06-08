package Kevin.Peyton.Game.Platform.Demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Kevin.Peyton.Game.Platform.Demo.entity.Library;
import Kevin.Peyton.Game.Platform.Demo.entity.LibraryId;

import java.util.List;

public interface LibraryRepository extends JpaRepository<Library, LibraryId> {
    
    @Query("SELECT l FROM Library l WHERE l.id.userId = :userId")
    List<Library> findByUserId(@Param("userId") Integer userId);
}
