package Kevin.Peyton.Game.Platform.Demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Kevin.Peyton.Game.Platform.Demo.entity.UserRole;
import Kevin.Peyton.Game.Platform.Demo.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser_Id(Integer userId);
    List<UserRole> findByRole_Id(Integer roleId);

    UserRole findByUser_IdAndRole_Id(Integer userId, Integer roleId);

    boolean existsByUser_IdAndRole_Id(Integer userId, Integer roleId);
}
