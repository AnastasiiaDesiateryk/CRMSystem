package org.example.crm.application.dto;

import java.util.List;

/**
 * Унифицированная страница результата. Не привязана к Spring Data Page,
 * чтобы application layer не зависел от фреймворка.
 */
public record PageResponse<T>(
        List<T> items,
        long total,
        int page,
        int size
) {}
