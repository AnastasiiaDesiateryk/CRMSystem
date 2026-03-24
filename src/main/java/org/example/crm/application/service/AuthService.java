package org.example.crm.application.service;



import org.example.crm.application.dto.*;
import org.example.crm.application.port.in.AuthServicePort;
import org.example.crm.application.port.out.*;
import org.example.crm.domain.model.RefreshToken;
import org.example.crm.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService implements AuthServicePort {

    // policy
    private static final long REFRESH_TOKEN_TTL_SECONDS = 60L * 60L * 24L * 30L; // 30 days

    private final UserStore userStore;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenHashing tokenHashing;
    private final Clock clock;

    public AuthService(
            UserStore userStore,
            RefreshTokenStore refreshTokenStore,
            PasswordHasher passwordHasher,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenGenerator refreshTokenGenerator,
            TokenHashing tokenHashing,
            Clock clock
    ) {
        this.userStore = userStore;
        this.refreshTokenStore = refreshTokenStore;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenHashing = tokenHashing;
        this.clock = clock;
    }

    @Transactional
    @Override
    public TokenResponse register(RegisterRequest request, String userAgent, String ip) {
        String email = normalizeEmail(request.getEmail());

        if (userStore.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("email_already_exists");
        }

        OffsetDateTime now = clock.now();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordHasher.hash(request.getPassword()));
        user.setName(request.getName());
        user.setRoles(Set.of("ROLE_USER"));
        user.setHasAccess(false); //  policy
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User saved = userStore.save(user);

        String access = accessTokenIssuer.issue(saved);
        String refresh = issueRefreshToken(saved.getId(), userAgent, ip, null);

        return new TokenResponse(access, refresh, accessTokenIssuer.accessTokenTtlSeconds());
    }

    @Transactional
    @Override
    public TokenResponse login(LoginRequest request, String userAgent, String ip) {
        String email = normalizeEmail(request.getEmail());

        User user = userStore.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("invalid_credentials"));

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid_credentials");
        }

        // optional: enforce access gate here or in controllers
        if (!user.isHasAccess()) {
            throw new IllegalArgumentException("access_denied");
        }

        String access = accessTokenIssuer.issue(user);
        String refresh = issueRefreshToken(user.getId(), userAgent, ip, null);

        return new TokenResponse(access, refresh, accessTokenIssuer.accessTokenTtlSeconds());
    }

    /**
     * Refresh token format: "<tokenId>.<raw>"
     * DB stores only tokenHash = sha256(raw)
     */
    @Transactional
    @Override
    public TokenResponse refresh(RefreshRequest request, String userAgent, String ip) {
        TokenParts parts = parseRefreshToken(request.getRefreshToken());

        RefreshToken token = refreshTokenStore.findById(parts.tokenId())
                .orElseThrow(() -> new IllegalArgumentException("invalid_refresh_token"));

        OffsetDateTime now = clock.now();

        // Reuse detection: if token already revoked -> assume theft/replay -> revoke all tokens for this user
        if (token.isRevoked()) {
            refreshTokenStore.revokeAllForUser(token.getUserId(), now);
            throw new IllegalArgumentException("refresh_token_reuse_detected");
        }

        if (token.getExpiresAt().isBefore(now)) {
            // expired token can be considered invalid; optionally revoke it
            refreshTokenStore.revokeById(token.getId(), now);
            throw new IllegalArgumentException("refresh_token_expired");
        }

        // Verify presented raw part matches stored hash
        String presentedHash = tokenHashing.sha256Hex(parts.raw());
        if (!presentedHash.equals(token.getTokenHash())) {
            // Someone is guessing/using wrong raw -> hard fail and revoke all as suspicious
            refreshTokenStore.revokeAllForUser(token.getUserId(), now);
            throw new IllegalArgumentException("invalid_refresh_token");
        }

        // Rotate: revoke current token, issue a new one referencing rotatedFromId
        refreshTokenStore.revokeById(token.getId(), now);

        UUID userId = token.getUserId();
        User user = userStore.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user_not_found"));

        if (!user.isHasAccess()) {
            throw new IllegalArgumentException("access_denied");
        }

        String newRefresh = issueRefreshToken(userId, userAgent, ip, token.getId());
        String access = accessTokenIssuer.issue(user);

        return new TokenResponse(access, newRefresh, accessTokenIssuer.accessTokenTtlSeconds());
    }

    @Transactional
    @Override
    public void logout(LogoutRequest request, String userAgent, String ip) {
        TokenParts parts = parseRefreshToken(request.getRefreshToken());

        // id-based revoke (cheap); raw verification optional here
        refreshTokenStore.findById(parts.tokenId()).ifPresent(rt -> {
            OffsetDateTime now = clock.now();
            // Optional: verify raw matches hash before revoking
            String presentedHash = tokenHashing.sha256Hex(parts.raw());
            if (presentedHash.equals(rt.getTokenHash()) && !rt.isRevoked()) {
                refreshTokenStore.revokeById(rt.getId(), now);
            }
        });
    }

    private String issueRefreshToken(UUID userId, String userAgent, String ip, UUID rotatedFromId) {
        OffsetDateTime now = clock.now();

        String raw = refreshTokenGenerator.generateRaw();
        String hash = tokenHashing.sha256Hex(raw);

        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID());
        rt.setUserId(userId);
        rt.setTokenHash(hash);
        rt.setCreatedAt(now);
        rt.setExpiresAt(now.plusSeconds(REFRESH_TOKEN_TTL_SECONDS));
        rt.setRotatedFromId(rotatedFromId);
        rt.setUserAgent(userAgent);
        rt.setIp(ip);

        RefreshToken saved = refreshTokenStore.save(rt);

        // Return "<id>.<raw>" to client
        return saved.getId() + "." + raw;
    }

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private TokenParts parseRefreshToken(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("invalid_refresh_token");
        int dot = value.indexOf('.');
        if (dot <= 0 || dot == value.length() - 1) throw new IllegalArgumentException("invalid_refresh_token");

        String idPart = value.substring(0, dot);
        String rawPart = value.substring(dot + 1);

        UUID id;
        try {
            id = UUID.fromString(idPart);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid_refresh_token");
        }
        return new TokenParts(id, rawPart);
    }

    private record TokenParts(UUID tokenId, String raw) {}
}
