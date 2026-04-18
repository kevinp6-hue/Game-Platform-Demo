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
    String passwordHash,
    OffsetDateTime dateJoined,
    Boolean isActive,
    OffsetDateTime lastLogin,
    List<Address> addresses,
    List<UserEmail> emails,
    List<UserRole> userRoles

) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getBirthDate(),
            user.getPasswordHash(),
            user.getDateJoined(),
            user.getIsActive(),
            user.getLastLogin(),
            user.getAddresses(),
            user.getEmails(),
            user.getUserRoles()

        );
    }
}
