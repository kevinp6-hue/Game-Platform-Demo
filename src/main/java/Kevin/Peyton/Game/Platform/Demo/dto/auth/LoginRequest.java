package Kevin.Peyton.Game.Platform.Demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials for login")
public record LoginRequest(
    @Schema(description = "Account username", example = "john_doe")
    @NotBlank
    @NotNull
    String username,

    @Schema(description = "Account password", example = "s3cr3tP@ss!")
    @NotBlank
    @NotNull
    String password
) {}
