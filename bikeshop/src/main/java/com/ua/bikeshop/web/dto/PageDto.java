package com.ua.bikeshop.web.dto;

import java.util.List;

/**
 * Одна сторінка переліку — контракт пагінованих ендпоінтів (ПЗ 2/3).
 *
 * ШАР: web/dto. Назовні віддається саме цей вузький запис, а не Spring-тип
 * {@code Page} цілком: у {@code Page} багато службових полів, а його JSON —
 * внутрішня деталь фреймворку, яка може змінитись між версіями. Тут рівно те,
 * що потрібно клієнту, щоб намалювати «Сторінка X з Y» і кнопки гортання.
 *
 * @param content       вміст сторінки
 * @param page          номер сторінки, з 0
 * @param size          запитаний розмір сторінки
 * @param totalElements скільки записів усього (з урахуванням фільтрів)
 * @param totalPages    скільки сторінок усього
 */
public record PageDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
