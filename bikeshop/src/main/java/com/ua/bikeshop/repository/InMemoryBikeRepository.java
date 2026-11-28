package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реалізація {@link BikeRepository} у пам'яті (заняття 1/1 — ще без БД).
 *
 * ШАР: repository (реалізація). Відповідає ЛИШЕ за зберігання й пошук (SRP):
 * наповнення стартовими даними винесено окремо — у {@code BikeSeeder}.
 *
 * Сховище — {@link ConcurrentHashMap} (кілька запитів Tomcat обслуговує
 * паралельно), ідентифікатори видає {@link AtomicLong}. Перелік повертається
 * упорядкованим за id: {@code ConcurrentHashMap} порядку не зберігає, а клієнт
 * має бачити стабільну послідовність (ПЗ 1/3).
 *
 * ПЗ 2/3: основним сховищем став PostgreSQL, а ця реалізація лишилась під
 * профілем {@code inmemory} — як демонстрація того, що верхні шари не залежать
 * від способу зберігання: запуск із {@code --spring.profiles.active=inmemory}
 * повертає застосунок у пам'ять, і жоден інший клас при цьому не змінюється.
 *
 * Сторінки, «знижки» й підрахунок за типом тут рахуються у самій Java —
 * у БД те саме роблять LIMIT/OFFSET, SQL-view і збережена функція. Порівняння
 * цих двох реалізацій добре показує, що саме бере на себе база.
 */
@Repository
@Profile("inmemory")
public class InMemoryBikeRepository implements BikeRepository {

    private final Map<Long, Bike> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public List<Bike> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Bike::getId))
                .toList();
    }

    @Override
    public List<Bike> search(String type, String color) {
        return storage.values().stream()
                .filter(bike -> type == null || type.equalsIgnoreCase(bike.getType()))
                .filter(bike -> color == null || color.equalsIgnoreCase(bike.getColor()))
                .sorted(Comparator.comparing(Bike::getId))
                .toList();
    }

    @Override
    public Page<Bike> search(String type, String color, Pageable pageable) {
        List<Bike> matched = search(type, color);
        List<Bike> ordered = matched.stream()
                .sorted(comparator(pageable.getSort()))
                .toList();

        // ручний зріз сторінки: від offset до offset+size, з оглядкою на межі переліку
        int from = (int) Math.min(pageable.getOffset(), ordered.size());
        int to = Math.min(from + pageable.getPageSize(), ordered.size());
        // третій аргумент PageImpl — ПОВНА кількість знайденого, а не розмір зрізу:
        // саме з неї рахуються totalElements і totalPages
        return new PageImpl<>(ordered.subList(from, to), pageable, ordered.size());
    }

    /**
     * Перетворює {@code ?sort=поле,напрям} на компаратор. Підтримано поля, за
     * якими справді сортують у переліку; усе інше (і порожній sort) — за id.
     * У реалізації на БД цього методу немає взагалі: там будь-яке поле сортує
     * сам PostgreSQL за ORDER BY, і це одна з причин, чому сховище в пам'яті
     * лишається лише навчальним.
     */
    private static Comparator<Bike> comparator(Sort sort) {
        Comparator<Bike> result = null;
        for (Sort.Order order : sort) {
            Comparator<Bike> next = switch (order.getProperty()) {
                case "model" -> Comparator.comparing(Bike::getModel, Comparator.nullsLast(String::compareTo));
                case "brand" -> Comparator.comparing(Bike::getBrand, Comparator.nullsLast(String::compareTo));
                case "type" -> Comparator.comparing(Bike::getType, Comparator.nullsLast(String::compareTo));
                case "price" -> Comparator.comparing(Bike::getPrice, Comparator.nullsLast(Comparable::compareTo));
                case "year" -> Comparator.comparing(Bike::getYear, Comparator.nullsLast(Comparable::compareTo));
                default -> Comparator.comparing(Bike::getId);
            };
            if (order.isDescending()) {
                next = next.reversed();
            }
            result = (result == null) ? next : result.thenComparing(next);
        }
        return result == null ? Comparator.comparing(Bike::getId) : result;
    }

    @Override
    public Optional<Bike> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Bike save(Bike bike) {
        if (bike.getId() == null) {
            bike.setId(sequence.incrementAndGet());
        }
        storage.put(bike.getId(), bike);
        return bike;
    }

    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    @Override
    public void deleteAll() {
        storage.clear();
        // лічильник навмисно не скидаємо: id не повинні повторно використовуватись
    }

    @Override
    public List<Bike> findDiscounted() {
        return storage.values().stream()
                .filter(bike -> bike.getPrice() != null
                        && bike.getPrice().compareTo(DISCOUNT_PRICE_LIMIT) < 0)
                .sorted(Comparator.comparing(Bike::getPrice))
                .toList();
    }

    @Override
    public Page<Bike> findDiscounted(Pageable pageable) {
        List<Bike> discounted = findDiscounted();
        int from = (int) Math.min(pageable.getOffset(), discounted.size());
        int to = Math.min(from + pageable.getPageSize(), discounted.size());
        return new PageImpl<>(discounted.subList(from, to), pageable, discounted.size());
    }

    @Override
    public long countByType(String type) {
        return storage.values().stream()
                .filter(bike -> type == null || type.equalsIgnoreCase(bike.getType()))
                .count();
    }
}
