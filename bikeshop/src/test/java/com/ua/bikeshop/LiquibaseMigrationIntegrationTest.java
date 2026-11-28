package com.ua.bikeshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Перевірка самих МІГРАЦІЙ (ПЗ 2/3).
 *
 * Тест читає базу напряму через JdbcTemplate — повз JPA, бо перевіряє не
 * поведінку застосунку, а те, що зробив із базою Liquibase: які changeSet
 * записані в журнал, чи створені таблиці й чи наповнений каталог.
 *
 * Цей клас НЕ успадковує AbstractIntegrationTest, а піднімає ВЛАСНИЙ контейнер.
 * Причина: решта інтеграційних тестів чистить сховище в @BeforeEach (вони мають
 * бути автономними), а предмет перевірки тут — саме СТАРТОВИЙ стан бази, який
 * створили міграції. На спільній базі результат залежав би від порядку тестів;
 * на власній — ні.
 */
@SpringBootTest
class LiquibaseMigrationIntegrationTest {

    static final PostgreSQLContainer<?> MIGRATIONS_DB = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        MIGRATIONS_DB.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MIGRATIONS_DB::getJdbcUrl);
        registry.add("spring.datasource.username", MIGRATIONS_DB::getUsername);
        registry.add("spring.datasource.password", MIGRATIONS_DB::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void changelogTableContainsAllOurChangeSets() {
        List<String> applied = jdbc.queryForList(
                "SELECT id FROM databasechangelog ORDER BY orderexecuted", String.class);

        assertThat(applied).containsExactly(
                "001-create-bikes",
                "002-seed-bikes",
                "003-seed-more-bikes",
                "004-create-discounted-view",
                "004-create-count-function",
                "005-fill-missing-photos",
                "006-fts-search-vector");
    }

    @Test
    void migrationsCreatedTablesViewAndFunction() {
        assertThat(tableExists("bikes")).isTrue();
        assertThat(tableExists("bike_images")).isTrue();
        // службові таблиці Liquibase теж живуть у нашій базі
        assertThat(tableExists("databasechangelog")).isTrue();
        assertThat(tableExists("databasechangeloglock")).isTrue();

        Boolean viewExists = jdbc.queryForObject(
                "SELECT count(*) > 0 FROM information_schema.views WHERE table_name = 'bikes_discounted'",
                Boolean.class);
        assertThat(viewExists).isTrue();

        Boolean functionExists = jdbc.queryForObject(
                "SELECT count(*) > 0 FROM pg_proc WHERE proname = 'count_bikes_by_type'",
                Boolean.class);
        assertThat(functionExists).isTrue();
    }

    @Test
    void seedMigrationsFilledCatalogAndShiftedIdentityCounter() {
        Long bikes = jdbc.queryForObject("SELECT count(*) FROM bikes", Long.class);
        assertThat(bikes).isEqualTo(26L);      // 20 із changeSet 002 + 6 із 003

        Long maxId = jdbc.queryForObject("SELECT max(id) FROM bikes", Long.class);
        assertThat(maxId).isEqualTo(26L);

        // лічильник IDENTITY зсунуто за останній вставлений id — наступний запис
        // через POST отримає 27, а не впаде на дублікаті первинного ключа
        Long nextId = jdbc.queryForObject(
                "SELECT nextval(pg_get_serial_sequence('bikes', 'id'))", Long.class);
        assertThat(nextId).isEqualTo(27L);
    }

    @Test
    void ftsMigrationCreatedColumnIndexAndTrigger() {
        // стовпець search_vector існує в базі, але НЕ оголошений у сутності Bike —
        // Hibernate у режимі validate перевіряє лише свої колонки, тож зайва не заважає
        Boolean columnExists = jdbc.queryForObject(
                "SELECT count(*) > 0 FROM information_schema.columns "
                        + "WHERE table_name = 'bikes' AND column_name = 'search_vector'",
                Boolean.class);
        assertThat(columnExists).isTrue();

        Boolean indexExists = jdbc.queryForObject(
                "SELECT count(*) > 0 FROM pg_indexes WHERE indexname = 'idx_bikes_search_vector'",
                Boolean.class);
        assertThat(indexExists).isTrue();

        Boolean triggerExists = jdbc.queryForObject(
                "SELECT count(*) > 0 FROM pg_trigger WHERE tgname = 'trg_bikes_search_vector'",
                Boolean.class);
        assertThat(triggerExists).isTrue();

        // міграція заповнила search_vector усім 26 велосипедам стартового каталогу
        Long withoutVector = jdbc.queryForObject(
                "SELECT count(*) FROM bikes WHERE search_vector IS NULL", Long.class);
        assertThat(withoutVector).isZero();
    }

    @Test
    void dataMigrationFilledPhotosForSecondBatch() {
        Long withoutPhoto = jdbc.queryForObject(
                "SELECT count(*) FROM bikes WHERE image_url IS NULL", Long.class);
        assertThat(withoutPhoto).isZero();     // міграція 005 довантажила фото

        Long withoutGallery = jdbc.queryForObject("""
                SELECT count(*) FROM bikes b
                WHERE NOT EXISTS (SELECT 1 FROM bike_images i WHERE i.bike_id = b.id)
                """, Long.class);
        assertThat(withoutGallery).isZero();
    }

    private boolean tableExists(String name) {
        Boolean exists = jdbc.queryForObject(
                "SELECT count(*) > 0 FROM information_schema.tables WHERE table_name = ?",
                Boolean.class, name);
        return Boolean.TRUE.equals(exists);
    }
}
