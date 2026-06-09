package Kevin.Peyton.Game.Platform.Demo.controller;

import java.time.Duration;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseCookie;

import Kevin.Peyton.Game.Platform.Demo.service.AuthService;
import Kevin.Peyton.Game.Platform.Demo.dto.auth.AuthResponse;
import Kevin.Peyton.Game.Platform.Demo.dto.auth.LoginRequest;
import Kevin.Peyton.Game.Platform.Demo.dto.auth.LoginResult;
import Kevin.Peyton.Game.Platform.Demo.dto.errors.ApiErrorResponse;


@Tag(name = "Authentication", description = "Obtain and rotate JWT access tokens via cookie-based refresh tokens")
@Validated
@RestController
@RequestMapping("/API/auth")
public class AuthController {
    private final AuthService authService;

    private final boolean cookieSecure;
    private final long refreshTtlDays;

    public AuthController(
            AuthService authService,
            @Value("${security.cookie.secure:false}") boolean cookieSecure,
            @Value("${security.jwt.refreshTtlDays}") long refreshTtlDays) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
        this.refreshTtlDays = refreshTtlDays;
    }


    @Operation(summary = "Login", description = "Authenticate with username and password. Returns a JWT access token and sets an HttpOnly refresh_token cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Bad credentials",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult loginResult = authService.login(request.username(), request.password());

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", loginResult.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/API/auth")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(loginResult.response());
    }

    @Operation(summary = "Refresh access token", description = "Exchange the refresh_token cookie for a new access token and a rotated refresh cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing, expired, or revoked refresh token",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        LoginResult loginResult = authService.refresh(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", loginResult.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/API/auth")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(loginResult.response());
    }

    @Operation(summary = "Logout", description = "Revoke the refresh token and clear the refresh_token cookie.")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        authService.logout(refreshToken);
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/API/auth")
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

}
