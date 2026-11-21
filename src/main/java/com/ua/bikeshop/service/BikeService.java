package com.ua.bikeshop.service;

import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;

import java.util.List;

/**
 * Контракт сервісу велосипедів (бізнес-логіка).
 *
 * ШАР: service (абстракція). Контролер залежить від цього інтерфейсу, а не від
 * реалізації — DIP. Це ж дає змогу підставити мок у тестах веб-шару.
 */
public interface BikeService {

    /** Перелік велосипедів у вигляді карток. */
    List<BikeCardDto> findAll();

    /** Один велосипед з повними деталями; кидає BikeNotFoundException, якщо немає. */
    BikeDetailDto findById(Long id);
}
