package Kevin.Peyton.Game.Platform.Demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;

import Kevin.Peyton.Game.Platform.Demo.dto.auth.AuthResponse;
import Kevin.Peyton.Game.Platform.Demo.entity.RefreshToken;
import Kevin.Peyton.Game.Platform.Demo.repository.RefreshTokenRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRepository;
import Kevin.Peyton.Game.Platform.Demo.repository.UserRoleRepository;
import jakarta.transaction.Transactional;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.accessTtlSeconds}")    
    private long accessTtlSeconds;

    @Value ("${security.jwt.refreshTtlDays}")
    private long refreshTtlDays;


    // Constructor injection for dependencies
    public AuthService(UserRepository userRepository,
         UserRoleRepository userRoleRepository, 
         RefreshTokenRepository refreshTokenRepository, 
         PasswordEncoder passwordEncoder,
         JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;

    }

    public record LoginResult(AuthResponse response, String refreshToken) {
    }

    @Transactional
    public LoginResult login(String username, String rawPassword) {
        // 1. Load user (Throw 401-style exception if missing)
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // 2. Verify password
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 3. Load roles and map to strings
        List<String> roles = userRoleRepository.findByUser_Id(user.getId()).stream()
            .map(ur -> ur.getRole().getName())
            .toList();

        // 4. Mint Access JWT
        var now = OffsetDateTime.now();
        var claims = JwtClaimsSet.builder()
                .issuer("game-platform-demo")
                .issuedAt(now.toInstant())
                .expiresAt(now.plusSeconds(accessTtlSeconds).toInstant())
                .subject(user.getUsername())
                .claim("roles", roles)
                .build();

        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        // 5. Mint Refresh Token
        String rawRefreshToken = generateRefreshToken();
        String hashedRefreshToken = hashRefreshToken(rawRefreshToken);

        // 6. Save Refresh Token row
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(hashedRefreshToken);
        refreshTokenEntity.setExpiresAt(now.plusDays(refreshTtlDays)); // Long-lived
    
        refreshTokenRepository.save(refreshTokenEntity);

        // 7. Return both tokens
        return new LoginResult(
                new AuthResponse(accessToken, "Bearer", accessTtlSeconds),
                rawRefreshToken);
}

    private String generateRefreshToken(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashRefreshToken(String raw){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash refresh token", ex);
        }
    }
}
