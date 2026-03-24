package org.example.crm.adapter.out.persistence.jpa;

import org.example.crm.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

//Это Spring Data JPA репозиторий — технический интерфейс для работы с БД.
//Его роль в гексагональной архитектуре
//❌ НЕ domain
//❌ НЕ application
//✅ adapter.out.persistence
//Он:
//знает про JPA
//знает про @Entity
//знает про SQL / Hibernate
//НЕ используется напрямую в application
//
//Почему он нужен
//Spring Data:
//сам генерирует SQL
//сам реализует findById, save, delete
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}
