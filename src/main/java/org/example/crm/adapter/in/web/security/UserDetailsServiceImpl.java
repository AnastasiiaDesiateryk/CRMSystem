package org.example.crm.adapter.in.web.security;

import org.example.crm.adapter.out.persistence.entity.UserEntity;
import org.example.crm.adapter.out.persistence.jpa.UserJpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Spring Security DAO authentication hook.
 *
 * Used by:
 * - DaoAuthenticationProvider in SecurityConfig
 *
 * Why it's NOT in application layer:
 * - depends on Spring Security interfaces
 * - technical adapter responsibility
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    public UserDetailsServiceImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity u = userJpaRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = u.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
