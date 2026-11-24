package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;

import java.util.List;
import java.util.Optional;

/**
 * Контракт сховища велосипедів.
 *
 * ШАР: repository (абстракція). Сервіс залежить від цього інтерфейсу, а не від
 * конкретної реалізації — DIP. Зараз за ним стоїть сховище в пам'яті, згодом
 * його замінить реалізація на базі даних, і сервіс від цього не зміниться.
 */
public interface BikeRepository {

    /** Усі велосипеди зі сховища. */
    List<Bike> findAll();

    /**
     * Велосипеди за критеріями переліку. Критерій зі значенням {@code null}
     * не застосовується, тому один метод покриває і фільтр за типом, і фільтр
     * за кольором, і обидва разом (ПЗ 1/3).
     */
    List<Bike> search(String type, String color);

    /** Велосипед за id або {@link Optional#empty()}, якщо такого немає. */
    Optional<Bike> findById(Long id);

    /** Зберігає велосипед: без id — створює новий, з id — перезаписує. */
    Bike save(Bike bike);

    /** Видаляє велосипед; повертає false, якщо такого id не було. */
    boolean deleteById(Long id);

    /**
     * Очищає сховище. Потрібен інтеграційним тестам (ГЗ 1/4), щоб кожен тест
     * починав з порожнього стану й не залежав від інших і від стартових даних.
     */
    void deleteAll();
}
