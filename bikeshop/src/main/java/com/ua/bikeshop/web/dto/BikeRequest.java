package com.ua.bikeshop.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Вхідні дані для СТВОРЕННЯ або ОНОВЛЕННЯ велосипеда (тіло запитів POST і PUT).
 *
 * ШАР: web/dto. Окремий від вихідних DTO контракт і навмисно БЕЗ поля {@code id}:
 * при створенні ідентифікатор призначає сервер, при оновленні він приходить
 * у шляху запиту. Так вхідний і вихідний контракти не змішуються, і клієнт
 * не може підмінити чужий id.
 */
public record BikeRequest(
        String model,
        String brand,
        String type,
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
