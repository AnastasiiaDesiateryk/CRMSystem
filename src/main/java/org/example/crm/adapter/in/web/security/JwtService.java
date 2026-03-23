package org.example.crm.adapter.in.web.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.example.crm.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Base64;

/**
 * TECHNICAL SERVICE (Web/Security).
 *
 * Responsibility:
 * - issue JWT access tokens (RS256)
 * - validate JWT signature + expiry
 * - provide JWKS (public keys) for /.well-known/jwks.json
 *
 * IMPORTANT:
 * - This is NOT business logic.
 * - It is pure security plumbing and stays outside application/domain.
 *
 * Related files:
 * - TokenFilter: uses validate() + parseClaims() to authenticate each request.
 * - JwksController: exposes getJwks() publicly.
 */
@Service
public class JwtService {

    private static final Logger LOG = Logger.getLogger(JwtService.class.getName());

    public static final long ACCESS_TOKEN_SECONDS = 15 * 60L;

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${jwt.keys.path:keys}") String keysPath,
            @Value("${jwt.issuer:http://localhost:8080}") String issuer,
            @Value("${jwt.audience:crm-frontend}") String audience
    ) throws Exception {
        this.issuer = issuer;
        this.audience = audience;

        try (InputStream privateStream = resource(keysPath + "/private.pem");
             InputStream publicStream  = resource(keysPath + "/public.pem")) {

            String privatePem = new String(privateStream.readAllBytes())
                    .replaceAll("-----\\w+ RSA PRIVATE KEY-----", "")
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            String publicPem = new String(publicStream.readAllBytes())
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] privateBytes = Base64.getDecoder().decode(privatePem);
            byte[] publicBytes  = Base64.getDecoder().decode(publicPem);

            this.privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            this.publicKey  = (RSAPublicKey)  kf.generatePublic(new X509EncodedKeySpec(publicBytes));
        }
    }

    private static InputStream resource(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) throw new IllegalStateException("RSA key file not found in classpath: " + path);
        return is;
    }

    /**
     * Creates a short-lived access token (RS256).
     *
     * Claims strategy (explicit and intentional):
     * - sub   : stable principal identifier used by SecurityContext (userId UUID)
     * - roles : authorization data for RBAC checks (ROLE_ADMIN / ROLE_USER)
     * - uid   : duplicated for convenience/debugging (optional)
     * - email : optional identity hint for UI/logs (never trust it for auth decisions)
     *
     * Rationale:
     * - RBAC decisions happen in Spring Security via GrantedAuthorities.
     * - We keep the token self-contained: no DB hit per request.
     * - "sub=userId" keeps principal immutable even if email changes.
     */
    public String generateAccessToken(User user) throws Exception {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ACCESS_TOKEN_SECONDS);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(user.getId().toString())
                .claim("uid", user.getId().toString())         // optional redundancy
                .claim("email", user.getEmail())               // optional UI-friendly field
                .claim("roles", user.getRoles() == null ? List.of() : new ArrayList<>(user.getRoles()))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                claims
        );

        jwt.sign(new RSASSASigner(privateKey));
        return jwt.serialize();
    }

    /**
     * JWKS payload for clients to validate your JWT signature.
     */
    public Map<String, Object> getJwks() {
        RSAKey key = new RSAKey.Builder(publicKey).build();
        return Map.of("keys", List.of(key.toPublicJWK().toJSONObject()));
    }

    /**
     * Optional: internal log helper (doesn't leak token).
     */
    void logInvalidToken(String path, Exception e) {
        LOG.log(Level.FINE, () -> "JWT invalid path=" + path + " reason=" + e.getClass().getSimpleName());
    }

    public JWTClaimsSet validateAndGetClaims(String token) throws Exception {
        SignedJWT jwt = SignedJWT.parse(token);

        boolean sigOk = jwt.verify(new RSASSAVerifier(publicKey));
        if (!sigOk) throw new IllegalArgumentException("jwt_bad_signature");

        JWTClaimsSet c = jwt.getJWTClaimsSet();

        Date exp = c.getExpirationTime();
        if (exp == null || !exp.after(new Date())) throw new IllegalArgumentException("jwt_expired");

        if (!issuer.equals(c.getIssuer())) throw new IllegalArgumentException("jwt_bad_issuer");

        List<String> aud = c.getAudience();
        if (aud == null || !aud.contains(audience)) throw new IllegalArgumentException("jwt_bad_audience");

        return c;
    }
}
