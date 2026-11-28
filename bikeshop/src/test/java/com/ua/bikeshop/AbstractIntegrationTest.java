package com.ua.bikeshop;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * База для інтеграційних тестів проти СПРАВЖНЬОГО PostgreSQL (ПЗ 2/3).
 *
 * Чому не H2 чи інша легка БД у пам'яті: вона лише схожа на PostgreSQL —
 * інакше поводиться з типами, діалектом, view й функціями. Тест на ній міг би
 * бути зеленим там, де робоча база впала б. Testcontainers піднімає той самий
 * образ postgres:16-alpine, що і в docker-compose.yml.
 *
 * Контейнер ОДИН на весь прогін (статичний, стартує в статичному блоці), а не
 * свій у кожного тест-класу: підняття БД — найдорожча частина, і спільний
 * контейнер прибирає як зайві секунди, так і конфлікти портів між контекстами.
 * Зупиняти його не треба — JVM прибирає контейнер сама наприкінці.
 *
 * Параметри підключення прокидаються в застосунок через @DynamicPropertySource:
 * порт у контейнера випадковий, тому його не можна прописати в application.yaml.
 * Схему в цій базі створює Liquibase — тести йдуть по тих самих міграціях, що й
 * робочий застосунок.
 */
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
