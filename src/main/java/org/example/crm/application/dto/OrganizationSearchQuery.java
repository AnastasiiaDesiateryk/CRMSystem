package org.example.crm.application.dto;

/**
 * Параметры поиска/фильтрации/пагинации для списка организаций.
 * q — общий поиск (name/email/category)
 * Остальные — точные фильтры.
 */
public record OrganizationSearchQuery(
        String q,
        String category,
        String status,
        String websiteStatus,
        Integer page,
        Integer size,
        String sort // например: "createdAt,desc" или "name,asc"
) {
    public OrganizationSearchQuery {
        // дефолты, чтобы контроллер мог не присылать всё
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0 || size > 200) size = 20;
        if (sort == null || sort.isBlank()) sort = "updatedAt,desc";
    }
}
