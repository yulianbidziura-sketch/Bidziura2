package com.ua.bikeshop.repository;

import com.ua.bikeshop.AbstractIntegrationTest;
import com.ua.bikeshop.domain.Bike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Інтеграційний тест доменного сховища проти реального PostgreSQL (ПЗ 2/3).
 *
 * Перевіряється САМЕ контракт {@link BikeRepository} — той, від якого залежить
 * сервіс, — але за ним тут стоїть JPA-адаптер і справжня база: фільтри
 * виконуються запитом, сторінки ріже LIMIT/OFFSET, «знижки» відбирає SQL-view,
 * підрахунок робить збережена функція.
 *
 * Тести автономні: @BeforeEach чистить сховище, кожен тест сам готує свої дані.
 */
@SpringBootTest
class BikeRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BikeRepository repository;

    /**
     * Потрібен одному тесту нижче: галерея фото — лінива колекція, і звернутись
     * до неї можна лише доки відкрита сесія. У застосунку цю межу дає
     * {@code @Transactional} на сервісі; тут сервісу немає, тому транзакцію
     * відкриваємо в тесті явно.
     */
    @Autowired
    private TransactionTemplate transactions;

    @BeforeEach
    void clearState() {
        repository.deleteAll();
    }

    @Test
    void saveAndFindById_persistsAllFieldsIncludingGallery() {
        Bike saved = repository.save(bike("Trail 29 Pro", "Гірський", "Чорний", "41900.00"));

        assertThat(saved.getId()).isNotNull();          // id видала база

        // читання — в окремій транзакції: так перевіряється справжній рейс до бази,
        // а не те, що об'єкт лишився в контексті персистентності після збереження
        transactions.executeWithoutResult(status -> {
            Bike found = repository.findById(saved.getId()).orElseThrow();
            assertThat(found.getModel()).isEqualTo("Trail 29 Pro");
            assertThat(found.getPrice()).isEqualByComparingTo("41900.00");
            assertThat(found.getImages())
                    .containsExactly("/images/bike-trail.svg", "/images/bike-trail-2.svg");
        });
    }

    @Test
    void search_appliesBothFiltersAndSkipsNullOnes() {
        repository.save(bike("Trail 29 Pro", "Гірський", "Чорний", "41900.00"));
        repository.save(bike("Trail 27 Sport", "Гірський", "Сірий", "28700.00"));
        repository.save(bike("Road Carbon 2", "Шосейний", "Чорний", "86500.00"));

        assertThat(repository.search(null, null)).hasSize(3);
        assertThat(repository.search("Гірський", null)).hasSize(2);
        assertThat(repository.search(null, "Чорний")).hasSize(2);
        assertThat(repository.search("Гірський", "Чорний"))
                .extracting(Bike::getModel)
                .containsExactly("Trail 29 Pro");
    }

    @Test
    void search_isCaseInsensitive() {
        repository.save(bike("Trail 29 Pro", "Гірський", "Чорний", "41900.00"));

        assertThat(repository.search("гірський", "чорний")).hasSize(1);
    }

    @Test
    void searchPage_returnsSliceWithTotalsOverWholeResult() {
        for (int i = 1; i <= 25; i++) {
            repository.save(bike("Модель " + i, "Гірський", "Чорний", "10000.00"));
        }

        Page<Bike> page = repository.search(null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);      // на сторінці — 10
        assertThat(page.getTotalElements()).isEqualTo(25);  // а всього — 25
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isZero();
    }

    @Test
    void searchPage_lastPageMayBeShorterAndOutOfRangePageIsEmpty() {
        for (int i = 1; i <= 25; i++) {
            repository.save(bike("Модель " + i, "Гірський", "Чорний", "10000.00"));
        }

        assertThat(repository.search(null, null, PageRequest.of(2, 10)).getContent()).hasSize(5);
        // сторінка за межами діапазону — не помилка, а порожній вміст із правильними лічильниками
        Page<Bike> beyond = repository.search(null, null, PageRequest.of(9, 10));
        assertThat(beyond.getContent()).isEmpty();
        assertThat(beyond.getTotalElements()).isEqualTo(25);
    }

    @Test
    void searchPage_sortsByRequestedField() {
        repository.save(bike("Дешевий", "Гірський", "Чорний", "10000.00"));
        repository.save(bike("Дорогий", "Гірський", "Чорний", "90000.00"));
        repository.save(bike("Середній", "Гірський", "Чорний", "50000.00"));

        Page<Bike> desc = repository.search(null, null,
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "price")));

        assertThat(desc.getContent()).extracting(Bike::getModel)
                .containsExactly("Дорогий", "Середній", "Дешевий");
    }

    @Test
    void searchPage_countsOnlyFilteredRows() {
        repository.save(bike("Trail 29 Pro", "Гірський", "Чорний", "41900.00"));
        repository.save(bike("Trail 27 Sport", "Гірський", "Сірий", "28700.00"));
        repository.save(bike("Road Carbon 2", "Шосейний", "Чорний", "86500.00"));

        Page<Bike> page = repository.search("Гірський", null, PageRequest.of(0, 1));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);   // не 3 — фільтр враховано й у підрахунку
    }

    @Test
    void findDiscounted_readsSqlViewWithPriceLimit() {
        repository.save(bike("Junior 24", "Підлітковий", "Синій", "9800.00"));
        repository.save(bike("City Lite 7", "Міський", "Бірюзовий", "15400.00"));
        repository.save(bike("Road Carbon 2", "Шосейний", "Чорний", "86500.00"));

        List<Bike> discounted = repository.findDiscounted();

        assertThat(discounted).extracting(Bike::getModel)
                .containsExactly("Junior 24", "City Lite 7");   // view віддає за ціною зростанням
        assertThat(discounted).allSatisfy(bike ->
                assertThat(bike.getPrice()).isLessThan(new BigDecimal("20000.00")));
    }

    @Test
    void findDiscountedPage_paginatesOverView() {
        for (int i = 1; i <= 7; i++) {
            repository.save(bike("Дешевий " + i, "Міський", "Білий", "12000.00"));
        }
        repository.save(bike("Road Carbon 2", "Шосейний", "Чорний", "86500.00"));

        Page<Bike> page = repository.findDiscounted(PageRequest.of(0, 4));

        assertThat(page.getContent()).hasSize(4);
        assertThat(page.getTotalElements()).isEqualTo(7);   // дорогий у view не потрапив
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void countByType_isCalculatedByStoredFunction() {
        repository.save(bike("Trail 29 Pro", "Гірський", "Чорний", "41900.00"));
        repository.save(bike("Trail 27 Sport", "Гірський", "Сірий", "28700.00"));
        repository.save(bike("Road Carbon 2", "Шосейний", "Чорний", "86500.00"));

        assertThat(repository.countByType("Гірський")).isEqualTo(2);
        assertThat(repository.countByType("гірський")).isEqualTo(2);   // функція ігнорує регістр
        assertThat(repository.countByType("Гравійний")).isZero();
        assertThat(repository.countByType(null)).isEqualTo(3);         // без типу — усі
    }

    @Test
    void deleteById_reportsWhetherRowExisted() {
        Bike saved = repository.save(bike("Trail 29 Pro", "Гірський", "Чорний", "41900.00"));

        assertThat(repository.deleteById(saved.getId())).isTrue();
        assertThat(repository.deleteById(saved.getId())).isFalse();    // удруге вже нічого видаляти
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void searchFullText_ranksBetterMatchesFirst() {
        repository.save(described("Trail 29 Pro", "Гірський велосипед з карбоновою рамою"));
        repository.save(described("City Lite 7", "Міський велосипед"));

        // перший документ містить слово «велосипед» і ще й «карбоновою»; за запитом із
        // двох слів знайдеться лише він, за одним словом — обидва, і попереду стане
        // той, у якого ts_rank вищий
        assertThat(repository.searchFullText("велосипед")).hasSize(2);
        assertThat(repository.searchFullText("карбоновою велосипед"))
                .extracting(Bike::getModel)
                .containsExactly("Trail 29 Pro");
    }

    @Test
    void searchFullText_returnsNothingForUnknownWord() {
        repository.save(described("Trail 29 Pro", "Гірський велосипед"));

        assertThat(repository.searchFullText("акваріум")).isEmpty();
    }

    private static Bike described(String model, String description) {
        Bike bike = bike(model, "Гірський", "Чорний", "41900.00");
        bike.setDescription(description);
        return bike;
    }

    private static Bike bike(String model, String type, String color, String price) {
        return new Bike(null, model, "Ridex", type, new BigDecimal(price), "Алюміній", "L",
                new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg", "/images/bike-trail-2.svg"),
                "Опис", 6, color);
    }
}
