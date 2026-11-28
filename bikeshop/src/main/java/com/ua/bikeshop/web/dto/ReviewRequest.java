package com.ua.bikeshop.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Вхідні дані для додавання відгуку (ГЗ 2/4).
 *
 * ШАР: web/dto. Як і {@code BikeRequest}, не має ні {@code id}, ні
 * {@code bikeId}, ні дати: ідентифікатор призначає Mongo, велосипед береться зі
 * шляху запиту, а час ставить сервер — клієнт не повинен мати змоги написати
 * відгук «заднім числом».
 */
public record ReviewRequest(
        @NotBlank(message = "ім'я автора обов'язкове")
        String author,

        @Min(value = 1, message = "оцінка не може бути меншою за 1")
        @Max(value = 5, message = "оцінка не може бути більшою за 5")
        int rating,

        @NotBlank(message = "текст відгуку обов'язковий")
        String text
) {
}
