package com.ua.bikeshop.repository.jpa;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.repository.BikeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Реалізація доменного {@link BikeRepository} поверх Spring Data JPA (ПЗ 2/3).
 *
 * ШАР: repository/jpa. Адаптер: реалізує НАШ інтерфейс сховища, делегуючи все
 * до {@link BikeJpaRepository}. Саме завдяки йому перехід з пам'яті на
 * PostgreSQL не торкнувся ні сервісу, ні контролера — вони як залежали від
 * {@code BikeRepository}, так і залежать. Це і є інверсія залежностей у дії.
 *
 * {@code @Primary}: реалізацій контракту в проєкті дві (ще лишилась стара, у
 * пам'яті), і ця анотація каже контейнеру, кого підставляти за замовчуванням.
 * {@code @Profile("!inmemory")} вимикає адаптер під профілем {@code inmemory},
 * де бази немає взагалі.
 */
@Repository
@Primary
@Profile("!inmemory")
public class JpaBikeRepositoryAdapter implements BikeRepository {

    private final BikeJpaRepository jpa;

    public JpaBikeRepositoryAdapter(BikeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Bike> findAll() {
        return jpa.findAll(org.springframework.data.domain.Sort.by("id"));
    }

    @Override
    public List<Bike> search(String type, String color) {
        return jpa.search(type, color);
    }

    @Override
    public Page<Bike> search(String type, String color, Pageable pageable) {
        return jpa.searchPage(type, color, pageable);
    }

    @Override
    public Optional<Bike> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Bike save(Bike bike) {
        return jpa.save(bike);
    }

    @Override
    public boolean deleteById(Long id) {
        // перевірка існування потрібна, щоб відрізнити успішне видалення від 404:
        // сам deleteById у Spring Data на відсутній id просто нічого не робить
        if (!jpa.existsById(id)) {
            return false;
        }
        jpa.deleteById(id);
        return true;
    }

    @Override
    public void deleteAll() {
        jpa.deleteAll();
    }

    @Override
    public List<Bike> findDiscounted() {
        return jpa.findDiscountedView();
    }

    @Override
    public Page<Bike> findDiscounted(Pageable pageable) {
        return jpa.findDiscountedViewPage(pageable);
    }

    @Override
    public long countByType(String type) {
        return jpa.countByTypeFunction(type);
    }

    @Override
    public List<Bike> searchFullText(String query) {
        return jpa.searchFullText(query);
    }
}
