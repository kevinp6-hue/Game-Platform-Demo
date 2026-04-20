package Kevin.Peyton.Game.Platform.Demo.dto.auth;


public record AuthResponse(
    String token,
    String tokenType,
    Long expiresInSeconds
) {
    
}
