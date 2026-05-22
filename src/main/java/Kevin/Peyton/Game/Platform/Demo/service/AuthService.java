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
import Kevin.Peyton.Game.Platform.Demo.dto.auth.LoginResult;

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

    @Transactional
    public void logout(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
            return;
        }

        String hash = hashRefreshToken(refreshTokenRaw);

        var tokenEntityOpt = refreshTokenRepository.findByTokenHash(hash);
        tokenEntityOpt.ifPresent(tokenEntity -> {
            tokenEntity.setRevokedAt(OffsetDateTime.now());
            refreshTokenRepository.save(tokenEntity);
        });
    }


    @Transactional
    public LoginResult refresh(String refreshTokenRaw) {
        if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        var now = OffsetDateTime.now();

        // 1. Hash incoming raw token and find matching row (Throw 401-style exception if missing/invalid)
        String hash = hashRefreshToken(refreshTokenRaw);

        var tokenEntity = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // 2. Validate status
        if (tokenEntity.getRevokedAt() != null) {
            throw new BadCredentialsException("Refresh token revoked");
        }
        if (!tokenEntity.getExpiresAt().isAfter(now)) {
            throw new BadCredentialsException("Refresh token expired");
        }

        var user = tokenEntity.getUser();

        // 3. Rotate refresh token (revoke old, create new, link old -> new)
        tokenEntity.setRevokedAt(now);

        var newRefreshTokenRaw = generateRefreshToken();
        var newRefreshTokenHash = hashRefreshToken(newRefreshTokenRaw);

        RefreshToken newTokenEntity = new RefreshToken();
        newTokenEntity.setUser(user);
        newTokenEntity.setTokenHash(newRefreshTokenHash);
        newTokenEntity.setExpiresAt(now.plusDays(refreshTtlDays));
        refreshTokenRepository.save(newTokenEntity);

        tokenEntity.setReplacedBy(newTokenEntity);
        refreshTokenRepository.save(tokenEntity);

        // 4. Mint new access JWT
        List<String> roles = userRoleRepository.findByUser_Id(user.getId()).stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        var claims = JwtClaimsSet.builder()
                .issuer("game-platform-demo")
                .issuedAt(now.toInstant())
                .expiresAt(now.plusSeconds(accessTtlSeconds).toInstant())
                .subject(user.getUsername())
                .claim("roles", roles)
                .build();

        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResult(
                new AuthResponse(accessToken, "Bearer", accessTtlSeconds),
                newRefreshTokenRaw);
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
