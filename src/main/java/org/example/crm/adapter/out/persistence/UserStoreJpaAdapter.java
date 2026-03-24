package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.UserEntity;
import org.example.crm.adapter.out.persistence.jpa.UserJpaRepository;
import org.example.crm.application.port.out.UserStore;
import org.example.crm.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Persistence adapter (hexagonal): implements application outbound port using JPA.
 *
 * Depends on:
 * - UserStore (application.port.out)  <- implements this
 * - UserJpaRepository / UserEntity    <- technical persistence
 *
 * DOES NOT:
 * - expose UserEntity outside adapter.out.persistence
 * - depend on web/security/controllers
 */
@Component
public class UserStoreJpaAdapter implements UserStore {

    private final UserJpaRepository repo;

    public UserStoreJpaAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(String emailLower) {
        if (emailLower == null || emailLower.isBlank()) return Optional.empty();
        return repo.findByEmail(emailLower.trim().toLowerCase()).map(UserStoreJpaAdapter::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) return Optional.empty();
        return repo.findById(id).map(UserStoreJpaAdapter::toDomain);
    }

    @Transactional
    @Override
    public User save(User user) {
        if (user == null) throw new IllegalArgumentException("user_required");
        if (user.getEmail() == null || user.getEmail().isBlank()) throw new IllegalArgumentException("email_required");
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) throw new IllegalArgumentException("password_hash_required");
        if (user.getName() == null || user.getName().isBlank()) throw new IllegalArgumentException("name_required");

        UUID id = user.getId() != null ? user.getId() : UUID.randomUUID();

        // если есть — обновляем; если нет — создаём
        UserEntity e = repo.findById(id).orElseGet(UserEntity::new);

        // IMPORTANT:
        // 1) email храним нормализованно (lowercase) — у тебя порт уже ожидает lower
        // 2) createdAt/updatedAt в БД есть defaults + trigger, но entity-поля у тебя non-null,
        //    поэтому при создании лучше выставить их (или сделать nullable).
        boolean isNew = e.getId() == null;

        e.setId(id);
        e.setEmail(user.getEmail().trim().toLowerCase());
        e.setPasswordHash(user.getPasswordHash());
        e.setName(user.getName().trim());
        e.setHasAccess(user.isHasAccess());

        Set<String> roles = user.getRoles() == null ? Set.of() : user.getRoles();
        e.setRoles(new HashSet<>(roles));

        OffsetDateTime now = OffsetDateTime.now();
        if (isNew) {
            // если у тебя в БД дефолт now(), можно и не ставить,
            // но тогда entity поля должны быть nullable.
            if (e.getCreatedAt() == null) e.setCreatedAt(now);
        }
        // updatedAt в БД триггером, но чтобы entity не падала на not-null до flush:
        if (e.getUpdatedAt() == null) e.setUpdatedAt(now);

        UserEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Transactional
    @Override
    public void setRoles(UUID userId, Set<String> roles) {
        if (userId == null) throw new IllegalArgumentException("user_id_required");
        if (roles == null) roles = Set.of();

        UserEntity u = repo.findById(userId).orElseThrow();
        u.setRoles(new HashSet<>(roles));
        repo.save(u);
    }


    /* =========================
       Mapping: Entity <-> Domain
       ========================= */

    private static User toDomain(UserEntity e) {
        User u = new User();
        u.setId(e.getId());
        u.setEmail(e.getEmail());
        u.setPasswordHash(e.getPasswordHash());
        u.setName(e.getName());
        u.setHasAccess(e.isHasAccess());
        u.setRoles(e.getRoles());

        // createdAt/updatedAt: домен OffsetDateTime
        u.setCreatedAt(e.getCreatedAt());
        u.setUpdatedAt(e.getUpdatedAt());

        return u;
    }

}
