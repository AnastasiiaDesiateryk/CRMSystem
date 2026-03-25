package org.example.crm.application.port.in;

import org.example.crm.application.dto.UserDto;

/**
 * INBOUND PORT (Hexagonal Architecture).
 *
 * Что это:
 * - Контракт (интерфейс) "что можно сделать" с точки зрения бизнес-приложения.
 * - Его видит и вызывает внешний мир через inbound adapter (HTTP Controller).
 * - Он НЕ знает ничего про Spring MVC, JPA Entity, репозитории и т.д.
 *
 * Кто вызывает:
 * - org.example.crm.adapter.in.web.controller.UserController
 *   (endpoint GET /api/me дергает userService.me()).
 *
 * Кто реализует:
 * - application service (например):
 *   org.example.crm.application.service.UserService implements UserServicePort
 *
 * Откуда берётся текущий пользователь:
 * - В идеале: из SecurityContext (JWT filter кладёт principal в SecurityContext).
 * - Чтобы не тащить Spring Security в application, делаем отдельный порт:
 *   org.example.crm.application.port.in.CurrentUserPort
 *   и его адаптер:
 *   org.example.crm.adapter.in.web.security.SecurityContextCurrentUserAdapter
 *
 * Возвращаемое значение:
 * - UserDto (application DTO), чтобы web-слой мог просто сериализовать ответ.
 */
public interface UserServicePort {

    /**
     * Возвращает текущего аутентифицированного пользователя (кто я).
     *
     * HTTP связка:
     * - UserController: GET /api/me
     *
     * Ожидаемое поведение:
     * - если пользователь НЕ аутентифицирован => бросаем AppException
     *   (обычно NotFoundException/UnauthorizedException — как ты решишь),
     *   а GlobalExceptionHandler переведет это в HTTP статус.
     */
    UserDto me();
}
