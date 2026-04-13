package Kevin.Peyton.Game.Platform.Demo.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
    
    /**
     * Handles validation errors and returns a structured API error response.
     * @param ex The exception containing validation error details.
     * @param request The HTTP request that caused the exception.
     * @return A ResponseEntity containing the API error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var errorResponse = new ApiErrorResponse(
            java.time.LocalDateTime.now(),
            400,
            "Bad Request",
            ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed"),
            request.getRequestURI() 
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles EntityNotFoundException and returns a structured API error response.
     * @param ex The exception indicating that an entity was not found.
     * @param request The HTTP request that caused the exception.
     * @return A ResponseEntity containing the API error response with a 404 status code.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        var errorResponse = new ApiErrorResponse(
            java.time.LocalDateTime.now(),
            404,
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    /**
     * Handles any uncaught exceptions and returns a structured API error response with a 500 status code.
     * @param ex The exception that was thrown.
     * @param request The HTTP request that caused the exception.
     * @return A ResponseEntity containing the API error response with a 500 status code.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request)
    {
        var errorResponse = new ApiErrorResponse(
            java.time.LocalDateTime.now(),
            500,
            "Internal Server Error",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(500).body(errorResponse);
    }
}
