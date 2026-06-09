package Kevin.Peyton.Game.Platform.Demo.dto.errors;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response body returned for 4xx and 5xx responses")
public record ApiErrorResponse(
    @Schema(description = "Time the error occurred")
    OffsetDateTime timestamp,

    @Schema(description = "HTTP status code", example = "404")
    int status,

    @Schema(description = "Short error label", example = "Not Found")
    String error,

    @Schema(description = "Detailed error message", example = "User not found: 99")
    String message,

    @Schema(description = "Request path that triggered the error", example = "/API/users/99")
    String path
) {}
