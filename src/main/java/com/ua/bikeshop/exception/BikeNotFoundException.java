package com.ua.bikeshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Велосипеда з таким id у сховищі немає.
 *
 * ШАР: exception. Анотація {@code @ResponseStatus(NOT_FOUND)} змушує Spring MVC
 * віддати клієнтові 404 — контролеру не потрібно нічого про це знати.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BikeNotFoundException extends RuntimeException {

    public BikeNotFoundException(Long id) {
        super("Велосипед з id=" + id + " не знайдено");
    }
}
