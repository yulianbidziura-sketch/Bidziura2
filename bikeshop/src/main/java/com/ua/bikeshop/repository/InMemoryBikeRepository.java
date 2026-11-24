package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;
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
 */
@Repository
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
}
