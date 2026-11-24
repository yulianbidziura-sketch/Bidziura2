package com.ua.bikeshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Вхідні дані для СТВОРЕННЯ або ОНОВЛЕННЯ велосипеда (тіло запитів POST і PUT).
 *
 * ШАР: web/dto. Окремий від вихідних DTO контракт і навмисно БЕЗ поля {@code id}:
 * при створенні ідентифікатор призначає сервер, при оновленні він приходить
 * у шляху запиту. Так вхідний і вихідний контракти не змішуються, і клієнт
 * не може підмінити чужий id.
 *
 * ГЗ 1/4: на полях стоять правила Bean Validation. Коли контролер позначає
 * тіло запиту {@code @Valid}, Spring MVC перевіряє їх ще до виклику сервісу і
 * при порушенні кидає {@code MethodArgumentNotValidException} — її ловить
 * глобальний обробник і віддає 400 з переліком проблем.
 */
public record BikeRequest(
        @NotBlank(message = "модель обов'язкова")
        String model,

        @NotBlank(message = "бренд обов'язковий")
        String brand,

        @NotBlank(message = "тип велосипеда обов'язковий")
        String type,

        @Positive(message = "ціна має бути додатною")
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

        @PositiveOrZero(message = "залишок не може бути від'ємним")
        Integer inStock,

        String color
) {
}
