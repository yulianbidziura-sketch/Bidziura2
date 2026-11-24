package com.ua.bikeshop.web.dto;

import java.util.List;

/**
 * Єдиний формат тіла помилки для всього API (ГЗ 1/4).
 *
 * ШАР: web/dto. Клієнт завжди отримує одну й ту саму структуру незалежно від
 * причини помилки, тому обробляти її можна в одному місці:
 *
 * <pre>
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Велосипед з id=999 не знайдено",
 *   "details": []
 * }
 * </pre>
 *
 * @param status  код стану HTTP
 * @param error   стандартна назва коду
 * @param message коротке пояснення для людини
 * @param details перелік конкретних проблем (для валідації); інакше порожній
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        List<String> details
) {
}
