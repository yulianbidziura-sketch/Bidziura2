package com.ua.bikeshop.repository;

import com.ua.bikeshop.domain.Bike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульний тест сховища в пам'яті (ПЗ 1/5).
 *
 * Найнижчий рівень піраміди: Spring не піднімається взагалі, клас створюється
 * звичайним {@code new}. Перевіряється те, за що відповідає саме репозиторій:
 * видача id, перезапис за id, фільтри, порядок переліку й видалення.
 *
 * ПЗ 2/3: сюди додано сторінки, «знижки» й підрахунок за типом — ті самі
 * операції, що в PostgreSQL виконують LIMIT/OFFSET, SQL-view і збережена
 * функція. Контракт один, тому й очікування від обох реалізацій однакові.
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

    @Test
    void searchPage_cutsRequestedSliceAndCountsWholeResult() {
        for (int i = 1; i <= 7; i++) {
            repository.save(bike(null, "Модель " + i, "Гірський", "Чорний"));
        }

        Page<Bike> page = repository.search(null, null, PageRequest.of(1, 3));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(7);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    void searchPage_lastPageIsShorterAndOutOfRangeIsEmpty() {
        for (int i = 1; i <= 7; i++) {
            repository.save(bike(null, "Модель " + i, "Гірський", "Чорний"));
        }

        assertThat(repository.search(null, null, PageRequest.of(2, 3)).getContent()).hasSize(1);
        assertThat(repository.search(null, null, PageRequest.of(5, 3)).getContent()).isEmpty();
    }

    @Test
    void searchPage_appliesSortAndFilterTogether() {
        repository.save(priced("Дешевий", "Гірський", "10000.00"));
        repository.save(priced("Дорогий", "Гірський", "90000.00"));
        repository.save(priced("Шосейний дорогий", "Шосейний", "95000.00"));

        Page<Bike> page = repository.search("Гірський", null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price")));

        assertThat(page.getContent()).extracting(Bike::getModel)
                .containsExactly("Дорогий", "Дешевий");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findDiscounted_returnsOnlyCheapOnesOrderedByPrice() {
        repository.save(priced("Junior 24", "Підлітковий", "9800.00"));
        repository.save(priced("City Lite 7", "Міський", "15400.00"));
        repository.save(priced("Road Carbon 2", "Шосейний", "86500.00"));

        assertThat(repository.findDiscounted()).extracting(Bike::getModel)
                .containsExactly("Junior 24", "City Lite 7");
    }

    @Test
    void countByType_countsMatchingBikesIgnoringCase() {
        repository.save(bike(null, "Trail 29 Pro", "Гірський", "Чорний"));
        repository.save(bike(null, "Trail 27 Sport", "Гірський", "Сірий"));
        repository.save(bike(null, "Road Carbon 2", "Шосейний", "Чорний"));

        assertThat(repository.countByType("гірський")).isEqualTo(2);
        assertThat(repository.countByType(null)).isEqualTo(3);
    }

    @Test
    void searchFullText_requiresAllWordsAndIgnoresCase() {
        Bike trail = bike(null, "Trail 29 Pro", "Гірський", "Чорний");
        trail.setDescription("Карбонова рама і дискові гальма");
        repository.save(trail);
        Bike city = bike(null, "City Lite 7", "Міський", "Білий");
        city.setDescription("Планетарна втулка і багажник");
        repository.save(city);

        assertThat(repository.searchFullText("рама")).hasSize(1);
        assertThat(repository.searchFullText("карбонова ГАЛЬМА")).hasSize(1);
        assertThat(repository.searchFullText("карбонова багажник")).isEmpty();
        assertThat(repository.searchFullText("   ")).isEmpty();
    }

    private Bike priced(String model, String type, String price) {
        Bike bike = bike(null, model, type, "Чорний");
        bike.setPrice(new BigDecimal(price));
        return bike;
    }

    private Bike bike(Long id, String model, String type, String color) {
        return new Bike(id, model, "Ridex", type, new BigDecimal("41900.00"), "Алюміній",
                "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, color);
    }
}
