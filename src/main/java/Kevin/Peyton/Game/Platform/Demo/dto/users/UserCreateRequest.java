package Kevin.Peyton.Game.Platform.Demo.dto.users;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import Kevin.Peyton.Game.Platform.Demo.entity.Address;
import Kevin.Peyton.Game.Platform.Demo.entity.UserEmail;
import Kevin.Peyton.Game.Platform.Demo.entity.UserRole;

public record UserCreateRequest(
    String username,
    LocalDate birthDate,
    String password,
    OffsetDateTime dateJoined,
    Boolean isActive,
    OffsetDateTime lastLogin,
    List<Address> addresses,
    List<UserEmail> emails,
    List<UserRole> userRoles

)
 {
    
}
