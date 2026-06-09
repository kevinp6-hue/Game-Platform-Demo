package Kevin.Peyton.Game.Platform.Demo.dto.users;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields required to register a new user account")
public record UserCreateRequest(
    @Schema(description = "Desired username (must be unique)", example = "john_doe")
    String username,

    @Schema(description = "Date of birth (ISO-8601)", example = "1995-06-15")
    LocalDate birthDate,

    @Schema(description = "Account password (min 8 characters recommended)", example = "s3cr3tP@ss!")
    String password
) {}
