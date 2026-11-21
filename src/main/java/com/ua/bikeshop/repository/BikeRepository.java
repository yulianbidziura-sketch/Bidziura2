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

    /** Велосипед за id або {@link Optional#empty()}, якщо такого немає. */
    Optional<Bike> findById(Long id);

    /** Зберігає велосипед: без id — створює новий, з id — перезаписує. */
    Bike save(Bike bike);
}
