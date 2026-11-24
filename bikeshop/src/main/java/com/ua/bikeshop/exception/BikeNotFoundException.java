package com.ua.bikeshop.exception;

/**
 * Велосипеда з таким id у сховищі немає.
 *
 * ШАР: exception. До ГЗ 1/4 тут стояла анотація {@code @ResponseStatus(NOT_FOUND)}
 * — Spring сам віддавав порожній 404. Тепер статус і тіло відповіді формує
 * глобальний обробник {@code GlobalExceptionHandler}, щоб усі помилки API мали
 * єдиний формат {@code ErrorResponse}.
 */
public class BikeNotFoundException extends RuntimeException {

    public BikeNotFoundException(Long id) {
        super("Велосипед з id=" + id + " не знайдено");
    }
}
