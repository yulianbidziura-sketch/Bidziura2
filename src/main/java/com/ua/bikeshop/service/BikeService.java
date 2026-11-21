package com.ua.bikeshop.service;

import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;

import java.util.List;

/**
 * Контракт сервісу велосипедів (бізнес-логіка).
 *
 * ШАР: service (абстракція). Контролер залежить від цього інтерфейсу, а не від
 * реалізації — DIP. Це ж дає змогу підставити мок у тестах веб-шару.
 *
 * Заняття 1/2: до читання додано створення, оновлення й видалення.
 */
public interface BikeService {

    /** Перелік велосипедів у вигляді карток; {@code type == null} — усі типи. */
    List<BikeCardDto> findAll(String type);

    /** Один велосипед з повними деталями; кидає BikeNotFoundException, якщо немає. */
    BikeDetailDto findById(Long id);

    /** Створює велосипед і повертає його з призначеним id. */
    BikeDetailDto create(BikeRequest request);

    /** Оновлює наявний велосипед; кидає BikeNotFoundException, якщо такого id немає. */
    BikeDetailDto update(Long id, BikeRequest request);

    /** Видаляє велосипед; кидає BikeNotFoundException, якщо такого id немає. */
    void delete(Long id);
}
