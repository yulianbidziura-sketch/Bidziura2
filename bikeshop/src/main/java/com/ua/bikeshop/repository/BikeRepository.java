package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Контракт сховища велосипедів.
 *
 * ШАР: repository (абстракція). Сервіс залежить від цього інтерфейсу, а не від
 * конкретної реалізації — DIP. Зараз за ним стоїть сховище в пам'яті, згодом
 * його замінить реалізація на базі даних, і сервіс від цього не зміниться.
 *
 * ПЗ 2/3: «згодом» настало — з'явилась друга реалізація, на PostgreSQL
 * ({@code JpaBikeRepositoryAdapter}), і сервіс із контролером справді не
 * змінились. Контракт розширено трьома групами методів: сторінковий перелік,
 * читання SQL-view і виклик збереженої функції.
 *
 * {@link Page}/{@link Pageable} — типи Spring Data, але вони описують саме
 * контракт «віддати сторінку», а не спосіб зберігання, тому лишаються на цій
 * межі; сховище в пам'яті реалізує їх своїми засобами.
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

    /**
     * ПЗ 2/3: той самий фільтр, але СТОРІНКОЮ. Порядок не зашитий у метод —
     * його несе {@code pageable} (параметр запиту {@code ?sort=}), а разом із
     * вмістом повертаються й метадані (скільки всього записів і сторінок).
     */
    Page<Bike> search(String type, String color, Pageable pageable);

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

    /**
     * ПЗ 2/3 (оглядово): велосипеди «зі знижкою» — дешевші за
     * {@link #DISCOUNT_PRICE_LIMIT}. У реалізації на PostgreSQL за цим методом
     * стоїть SQL-view {@code bikes_discounted}, створений міграцією, тобто
     * вибірку робить сама база.
     */
    List<Bike> findDiscounted();

    /** Те саме сторінками — пагінація поверх view. */
    Page<Bike> findDiscounted(Pageable pageable);

    /**
     * ПЗ 2/3 (оглядово): скільки велосипедів цього типу. У реалізації на
     * PostgreSQL рахує збережена ФУНКЦІЯ {@code count_bikes_by_type(text)} —
     * обчислення виконує БД, застосунок лише читає число.
     */
    long countByType(String type);

    /** Межа «знижки»: усе, що дешевше, вважається акційним. */
    java.math.BigDecimal DISCOUNT_PRICE_LIMIT = new java.math.BigDecimal("20000.00");
}
