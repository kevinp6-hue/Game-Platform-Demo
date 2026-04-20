package Kevin.Peyton.Game.Platform.Demo.dto.users;

import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import Kevin.Peyton.Game.Platform.Demo.entity.User;
import Kevin.Peyton.Game.Platform.Demo.entity.UserRole;
import Kevin.Peyton.Game.Platform.Demo.entity.Address;
import Kevin.Peyton.Game.Platform.Demo.entity.UserEmail;

public record UserResponse(
    Integer id,
    String username,
    LocalDate birthDate,
    OffsetDateTime dateJoined,
    Boolean isActive,
    OffsetDateTime lastLogin

) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getBirthDate(),
            user.getDateJoined(),
            user.getIsActive(),
            user.getLastLogin()

        );
    }
}
