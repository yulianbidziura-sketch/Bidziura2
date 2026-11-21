package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
 * паралельно), ідентифікатори видає {@link AtomicLong}.
 */
@Repository
public class InMemoryBikeRepository implements BikeRepository {

    private final Map<Long, Bike> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public List<Bike> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Bike> findByType(String type) {
        return storage.values().stream()
                .filter(bike -> type.equalsIgnoreCase(bike.getType()))
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
}
