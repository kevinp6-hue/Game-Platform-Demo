package Kevin.Peyton.Game.Platform.Demo.exception;

import java.time.OffsetDateTime;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;

import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;

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
            OffsetDateTime.now(),
            400,
            "Bad Request",
            ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
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
            OffsetDateTime.now(),
            404,
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    /**
     * Handles HttpMessageNotReadableException and returns a structured API error response with a 400 status code.
     * @param ex The exception indicating that the HTTP message was not readable.
     * @param request The HTTP request that caused the exception.
     * @return A ResponseEntity containing the API error response with a 400 status code.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        var errorResponse = new ApiErrorResponse(
            OffsetDateTime.now(),
            400,
            "Bad Request",
            "Invalid request body",
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles ConstraintViolationException and returns a structured API error response with a 400 status code.
     * @param ex The exception containing constraint violation details.
     * @param request The HTTP request that caused the exception.
     * @return A ResponseEntity containing the API error response with a 400 status code.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        var errorResponse = new ApiErrorResponse(
            OffsetDateTime.now(),
            400,
            "Bad Request",
            ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Constraint violation"),
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles ConflictException and returns a structured API error response with a 409 status code.
     * @param ex The exception indicating a request conflict.
     * @param request The HTTP request that caused the exception.
     * @return A ResponseEntity containing the API error response with a 409 status code.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        var errorResponse = new ApiErrorResponse(
            OffsetDateTime.now(),
            409,
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(409).body(errorResponse);
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
            OffsetDateTime.now(),
            500,
            "Internal Server Error",
            "Unexpected error occurred",
            request.getRequestURI()
        );
        return ResponseEntity.status(500).body(errorResponse);
    }
}
