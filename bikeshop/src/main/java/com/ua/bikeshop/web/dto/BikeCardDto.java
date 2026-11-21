package com.ua.bikeshop.web.dto;

import java.math.BigDecimal;

/**
 * Картка велосипеда для ПЕРЕЛІКУ (GET /api/bikes).
 *
 * ШАР: web/dto. Мінімум полів — рівно те, що видно у списку: модель, бренд,
 * тип, ціна й головне фото. Опис, характеристики й галерею в перелік не віддаємо,
 * щоб не гнати мережею зайве.
 */
public record BikeCardDto(
        Long id,
        String model,
        String brand,
        String type,
        BigDecimal price,
        String imageUrl
) {
}
