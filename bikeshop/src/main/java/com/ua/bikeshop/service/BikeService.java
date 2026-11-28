package com.ua.bikeshop.service;

import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import com.ua.bikeshop.web.dto.PageDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Контракт сервісу велосипедів (бізнес-логіка).
 *
 * ШАР: service (абстракція). Контролер залежить від цього інтерфейсу, а не від
 * реалізації — DIP. Це ж дає змогу підставити мок у тестах веб-шару.
 *
 * Заняття 1/2: до читання додано створення, оновлення й видалення.
 * ПЗ 2/3: додано сторінковий перелік, читання SQL-view і виклик функції БД.
 */
public interface BikeService {

    /**
     * Перелік велосипедів у вигляді карток. Обидва фільтри необов'язкові:
     * порожній або відсутній критерій означає «усі» (ПЗ 1/3 — додано колір).
     */
    List<BikeCardDto> findAll(String type, String color);

    /**
     * ПЗ 2/3: перелік СТОРІНКОЮ. Фільтри ті самі й так само необов'язкові, а
     * номер сторінки, її розмір і порядок несе {@code pageable} — Spring збирає
     * його з параметрів {@code ?page=&size=&sort=}.
     */
    PageDto<BikeCardDto> findPage(String type, String color, Pageable pageable);

    /** ПЗ 2/3 (оглядово): велосипеди «зі знижкою» — їх відбирає SQL-view у базі. */
    List<BikeCardDto> findDiscounted();

    /** Те саме сторінками — пагінація поверх view. */
    PageDto<BikeCardDto> findDiscountedPage(Pageable pageable);

    /** ПЗ 2/3 (оглядово): скільки велосипедів цього типу — рахує функція в БД. */
    long countByType(String type);

    /**
     * ГЗ 2/4: повнотекстовий пошук за словами (модель, бренд, тип, опис).
     * Порожній запит — порожній результат, а не весь каталог.
     */
    List<BikeCardDto> search(String query);

    /** Один велосипед з повними деталями; кидає BikeNotFoundException, якщо немає. */
    BikeDetailDto findById(Long id);

    /** Створює велосипед і повертає його з призначеним id. */
    BikeDetailDto create(BikeRequest request);

    /** Оновлює наявний велосипед; кидає BikeNotFoundException, якщо такого id немає. */
    BikeDetailDto update(Long id, BikeRequest request);

    /** Видаляє велосипед; кидає BikeNotFoundException, якщо такого id немає. */
    void delete(Long id);
}
