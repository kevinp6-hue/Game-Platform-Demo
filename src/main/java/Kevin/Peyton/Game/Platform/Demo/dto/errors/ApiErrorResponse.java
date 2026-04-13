package Kevin.Peyton.Game.Platform.Demo.dto.errors;

import java.time.LocalDateTime;

/**
 * A record representing an API error response.
 * @value timestamp The time the error occurred.
 * @value status The HTTP status code of the error.
 * @value error A brief description of the error.
 * @value message A detailed message about the error.
 * @value path The URI path that caused the error.
 */
public record ApiErrorResponse(LocalDateTime timestamp, 
    int status, 
    String error, 
    String message, 
    String path) {
    
}
