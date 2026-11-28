package com.ua.bikeshop.web.dto;

import java.time.Instant;

/**
 * Відгук у відповіді API (ГЗ 2/4).
 *
 * ШАР: web/dto. Назовні йде DTO, а не документ {@code Review} — з тієї самої
 * причини, що й з велосипедами: контракт API не має повторювати внутрішню
 * модель. Зокрема, ідентифікатор документа Mongo віддається як звичайний рядок.
 */
public record ReviewDto(
        String id,
        Long bikeId,
        String author,
        int rating,
        String text,
        Instant createdAt
) {
}
