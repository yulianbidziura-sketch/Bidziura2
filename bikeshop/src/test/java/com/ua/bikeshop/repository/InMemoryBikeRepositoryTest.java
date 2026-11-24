package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульний тест сховища в пам'яті (ПЗ 1/5).
 *
 * Найнижчий рівень піраміди: Spring не піднімається взагалі, клас створюється
 * звичайним {@code new}. Перевіряється те, за що відповідає саме репозиторій:
 * видача id, перезапис за id, фільтри, порядок переліку й видалення.
 */
class InMemoryBikeRepositoryTest {

    private BikeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBikeRepository();
    }

    @Test
    void save_assignsIdWhenItIsMissing() {
        Bike saved = repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний"));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void save_withExistingIdOverwritesRecord() {
        Long id = repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний")).getId();

        repository.save(bike(id, "Trail 29 Pro 2025", "Гірський", "Синій"));

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findById(id)).get()
                .extracting(Bike::getModel, Bike::getColor)
                .containsExactly("Trail 29 Pro 2025", "Синій");
    }

    @Test
    void findAll_returnsBikesOrderedById() {
        repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний"));
        repository.save(bike(null, "Road Carbon 2", "Шосейний", "Червоний"));
        repository.save(bike(null, "City Lite 7", "Міський", "Чорний"));

        assertThat(repository.findAll())
                .extracting(Bike::getId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void search_appliesOnlyGivenCriteriaAndIgnoresCase() {
        repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний"));
        repository.save(bike(null, "Road Carbon 2", "Шосейний", "Червоний"));
        repository.save(bike(null, "City Lite 7", "Міський", "Чорний"));

        assertThat(repository.search("гірський", null)).hasSize(1);          // лише тип, інший регістр
        assertThat(repository.search(null, "ЧОРНИЙ")).hasSize(2);            // лише колір
        assertThat(repository.search("Міський", "Чорний")).hasSize(1);       // обидва критерії
        assertThat(repository.search("Міський", "Червоний")).isEmpty();      // несумісна пара
        assertThat(repository.search(null, null)).hasSize(3);                // жодного критерію
    }

    @Test
    void deleteById_tellsWhetherRecordExisted() {
        Long id = repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний")).getId();

        assertThat(repository.deleteById(id)).isTrue();
        assertThat(repository.deleteById(id)).isFalse();     // удруге вже нічого видаляти
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void deleteAll_emptiesStorageButKeepsIdSequence() {
        repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний"));
        repository.save(bike(null, "Road Carbon 2", "Шосейний", "Червоний"));

        repository.deleteAll();

        assertThat(repository.findAll()).isEmpty();
        // id не починаються заново: наступний запис отримує 3, а не 1
        assertThat(repository.save(bike(null, "City Lite 7", "Міський", "Чорний")).getId())
                .isEqualTo(3L);
    }

    private Bike bike(Long id, String model, String type, String color) {
        return new Bike(id, model, "Ridex", type, new BigDecimal("41900.00"), "Алюміній",
                "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, color);
    }
}
