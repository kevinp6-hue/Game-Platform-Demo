package Kevin.Peyton.Game.Platform.Demo.controller;

import java.time.Duration;

import jakarta.validation.Valid;

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


    /**
    * Endpoint for user login. Validates the provided credentials and returns an authentication response along with a refresh token.
    * @param request The login request containing the username and password.
    * @return A ResponseEntity containing the authentication response and refresh token if successful, or an error response if authentication fails.
    */
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
