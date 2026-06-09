package Kevin.Peyton.Game.Platform.Demo.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token returned after successful authentication or token refresh")
public record AuthResponse(
    @Schema(description = "Signed JWT access token")
    String token,

    @Schema(description = "Token type — always 'Bearer'", example = "Bearer")
    String tokenType,

    @Schema(description = "Seconds until the access token expires", example = "900")
    Long expiresInSeconds
) {}
