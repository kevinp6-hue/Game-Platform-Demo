package Kevin.Peyton.Game.Platform.Demo.dto.users;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import Kevin.Peyton.Game.Platform.Demo.entity.User;

@Schema(description = "Public user profile")
public record UserResponse(
    @Schema(description = "Unique user ID", example = "42")
    Integer id,

    @Schema(description = "Username", example = "john_doe")
    String username,

    @Schema(description = "Date of birth", example = "1995-06-15")
    LocalDate birthDate,

    @Schema(description = "Account creation timestamp")
    OffsetDateTime dateJoined,

    @Schema(description = "Whether the account is active", example = "true")
    Boolean isActive,

    @Schema(description = "Timestamp of the last login")
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
