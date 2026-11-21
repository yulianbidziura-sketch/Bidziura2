package com.ua.bikeshop.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Повні дані велосипеда для СТОРІНКИ ТОВАРУ (GET /api/bikes/{id}).
 *
 * ШАР: web/dto. Окремий від {@link BikeCardDto} контракт: тут уже всі
 * характеристики рами й трансмісії, опис і галерея фото.
 *
 * DTO, а не доменний Bike: домен можна змінювати всередині, не ламаючи
 * зовнішній контракт API.
 */
public record BikeDetailDto(
        Long id,
        String model,
        String brand,
        BigDecimal price,
        String frameMaterial,
        String frameSize,
        BigDecimal wheelSize,
        Integer gears,
        BigDecimal weightKg,
        Integer year,
        String imageUrl,
        List<String> images,
        String description,
        Integer inStock
) {
}
