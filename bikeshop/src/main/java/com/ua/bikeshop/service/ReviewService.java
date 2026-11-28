package com.ua.bikeshop.service;

import com.ua.bikeshop.web.dto.ReviewDto;
import com.ua.bikeshop.web.dto.ReviewRequest;

import java.util.List;

/**
 * Контракт сервісу відгуків (ГЗ 2/4).
 *
 * ШАР: service (абстракція) — так само, як {@code BikeService}: контролер
 * залежить від інтерфейсу, а в тестах веб-шару замість нього стає мок.
 */
public interface ReviewService {

    /** Відгуки про велосипед, найновіші спершу; 404, якщо велосипеда немає. */
    List<ReviewDto> findByBike(Long bikeId);

    /** Додає відгук; 404, якщо велосипеда з таким id немає. */
    ReviewDto add(Long bikeId, ReviewRequest request);
}
