package org.example.crm.adapter.in.web.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Inbound Security Filter (HTTP adapter).
 *
 * Responsibility:
 * - reads "Authorization: Bearer <token>"
 * - validates token via JwtService
 * - if valid: sets Authentication into SecurityContext
 *
 * Contract with CurrentUserIdPort adapter:
 * - we set Authentication principal = JWT subject (UUID string)
 * - therefore auth.getName() returns UUID string
 *
 * Related files:
 * - JwtService: validate() + parseClaims()
 * - SecurityContextCurrentUserIdAdapter: reads auth.getName() -> UUID
 */
public class TokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public TokenFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            try {
                JWTClaimsSet claims = jwtService.validateAndGetClaims(token);

                String subject = claims.getSubject(); // UUID string
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                Object rolesRaw = claims.getClaim("roles");
                if (rolesRaw instanceof List<?> list) {
                    for (Object r : list) {
                        if (r != null) authorities.add(new SimpleGrantedAuthority(r.toString()));
                    }
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ex) {
                // invalid token -> ignore, continue unauthenticated
                jwtService.logInvalidToken(request.getRequestURI(), ex);
            }
        }

        filterChain.doFilter(request, response);
    }
}
