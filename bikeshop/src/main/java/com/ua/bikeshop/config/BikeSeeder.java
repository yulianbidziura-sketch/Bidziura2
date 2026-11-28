package com.ua.bikeshop.config;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.repository.BikeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Наповнює сховище стартовими велосипедами під час запуску застосунку,
 * щоб перелік не був порожнім одразу після старту.
 *
 * ШАР: config. Винесено окремо від репозиторію (SRP): репозиторій зберігає,
 * сидер наповнює. Залежить від інтерфейсу {@link BikeRepository}, тому після
 * переходу на базу даних цей клас не змінюватиметься.
 *
 * Заняття 1/2: у стартових даних заповнено поле {@code type} — на ньому
 * працює фільтр переліку (п'ять велосипедів п'яти різних типів).
 *
 * ПЗ 1/3: у кожного велосипеда заповнено ще й {@code color} — п'ять різних
 * кольорів, щоб було видно роботу другого фільтра (?color=) і обох разом.
 *
 * ПЗ 2/3: стартові дані для PostgreSQL створює тепер МІГРАЦІЯ Liquibase —
 * вони так само під версіями, як і схема, і не залежать від того, чи
 * запустився якийсь компонент застосунку. Сидер лишається й працює лише під
 * профілем {@code inmemory}, де бази немає й наповнювати сховище більше нікому.
 */
@Component
@Profile("inmemory")
public class BikeSeeder implements ApplicationRunner {

    private final BikeRepository repository;

    public BikeSeeder(BikeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        repository.save(new Bike(null, "Trail 29 Pro", "Ridex", "Гірський", new BigDecimal("41900.00"),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg", "/images/bike-trail-2.svg"),
                "Гірський велосипед на алюмінієвій рамі з повітряною вилкою 120 мм "
                        + "і гідравлічними дисковими гальмами. Трансмісія 1×12.",
                6, "Чорний"));

        repository.save(new Bike(null, "Road Carbon 2", "Velora", "Шосейний", new BigDecimal("86500.00"),
                "Карбон", "M", new BigDecimal("28.0"), 22, new BigDecimal("8.20"), 2025,
                "/images/bike-road.svg",
                List.of("/images/bike-road.svg"),
                "Шосейний велосипед із карбоновою рамою й вилкою, аеродинамічний кермо-винос, "
                        + "обід-карбон 40 мм. Для швидких групових заїздів і стартів.",
                3, "Червоний"));

        repository.save(new Bike(null, "City Lite 7", "Urbano", "Міський", new BigDecimal("15400.00"),
                "Алюміній", "M", new BigDecimal("28.0"), 7, new BigDecimal("14.80"), 2024,
                "/images/bike-city.svg",
                List.of("/images/bike-city.svg"),
                "Міський велосипед із заниженою рамою, планетарною втулкою на 7 швидкостей, "
                        + "крилами й багажником у комплекті.",
                11, "Бірюзовий"));

        repository.save(new Bike(null, "Gravel One", "Gravix", "Гравійний", new BigDecimal("54300.00"),
                "Хромомолібденова сталь", "L", new BigDecimal("28.0"), 11, new BigDecimal("10.60"), 2025,
                "/images/bike-gravel.svg",
                List.of("/images/bike-gravel.svg"),
                "Гравійний велосипед на стальній рамі з кріпленнями під багажник і флягу, "
                        + "покришки 45 мм, кермо з розкриттям 16°.",
                4, "Пісочний"));

        repository.save(new Bike(null, "Junior 24", "Kidrex", "Підлітковий", new BigDecimal("9800.00"),
                "Алюміній", "S", new BigDecimal("24.0"), 8, new BigDecimal("11.20"), 2024,
                "/images/bike-junior.svg",
                List.of("/images/bike-junior.svg"),
                "Підлітковий велосипед на колесах 24 дюйми, полегшена рама, "
                        + "механічні дискові гальма з короткими ручками під дитячу долоню.",
                9, "Синій"));
    }
}
