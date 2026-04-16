package Kevin.Peyton.Game.Platform.Demo.exception;

/**
 * Indicates the request conflicts with the current state of the resource.
 * Intended to be mapped to HTTP 409 (Conflict).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

