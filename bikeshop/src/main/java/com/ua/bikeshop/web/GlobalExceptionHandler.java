package com.ua.bikeshop.web;

import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Глобальна обробка помилок API (ГЗ 1/4).
 *
 * ШАР: web. {@code @RestControllerAdvice} робить ці обробники спільними для
 * ВСІХ контролерів: помилки опрацьовуються в одному місці, а контролер лишається
 * чистим — він не знає ні про коди помилок, ні про формат тіла.
 *
 * Обробляються два сценарії:
 * <ul>
 *   <li>{@link BikeNotFoundException} — немає велосипеда з таким id → 404;</li>
 *   <li>{@link MethodArgumentNotValidException} — тіло запиту не пройшло
 *       перевірку {@code @Valid} → 400 з переліком конкретних проблем.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Немає такого велосипеда — 404 зі структурованим тілом. */
    @ExceptionHandler(BikeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(BikeNotFoundException ex) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                List.of());
    }

    /** Невалідне тіло запиту — 400 і перелік полів, які його не пройшли. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()                     // стабільний порядок — зручно перевіряти в тестах
                .toList();
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Помилка валідації вхідних даних",
                details);
    }
}
