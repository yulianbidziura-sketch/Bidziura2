package com.ua.bikeshop.repository.jpa;

import com.ua.bikeshop.domain.Bike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Репозиторій Spring Data JPA для {@link Bike} (ПЗ 2/3).
 *
 * ШАР: repository/jpa. Це НЕ доменний контракт — доменний лишився колишнім
 * ({@code BikeRepository}), а цей інтерфейс існує лише як інструмент доступу до
 * БД. Поєднує їх адаптер {@link JpaBikeRepositoryAdapter}.
 *
 * Жодного коду тут немає навмисно: findAll, findById, save, deleteById і
 * deleteAll Spring Data генерує сам, а решту описано запитами.
 */
public interface BikeJpaRepository extends JpaRepository<Bike, Long> {

    /**
     * Похідний метод (derived query): SQL будується з самої назви —
     * «знайти за типом без урахування регістру». Його тут лишено як зразок
     * найпростішого способу фільтрації; у переліку працює запит нижче, бо
     * фільтрів два й обидва необов'язкові.
     */
    List<Bike> findByTypeIgnoreCase(String type);

    /**
     * Перелік із двома НЕОБОВ'ЯЗКОВИМИ фільтрами. Прийом «(:param IS NULL OR ...)»
     * дає один запит замість чотирьох комбінацій: якщо параметр не заданий,
     * умова просто не впливає на вибірку.
     *
     * Порядок за id зашито свідомо — цей ендпоінт лишився з Модуля 1, де клієнт
     * розраховує на стабільну послідовність.
     */
    @Query("""
            SELECT b FROM Bike b
            WHERE (:type IS NULL OR LOWER(b.type) = LOWER(CAST(:type AS string)))
              AND (:color IS NULL OR LOWER(b.color) = LOWER(CAST(:color AS string)))
            ORDER BY b.id
            """)
    List<Bike> search(@Param("type") String type, @Param("color") String color);

    /**
     * Те саме СТОРІНКОЮ. Відмінності від {@link #search}:
     * немає ORDER BY — порядок задає {@code Pageable} (параметр {@code ?sort=}),
     * інакше зашите сортування конфліктувало б із побажанням клієнта;
     * для {@link Page} потрібен окремий запит підрахунку — {@code countQuery} за
     * тими самими умовами, інакше загальна кількість рахувалась би не по фільтру.
     */
    @Query(value = """
            SELECT b FROM Bike b
            WHERE (:type IS NULL OR LOWER(b.type) = LOWER(CAST(:type AS string)))
              AND (:color IS NULL OR LOWER(b.color) = LOWER(CAST(:color AS string)))
            """,
            countQuery = """
            SELECT count(b) FROM Bike b
            WHERE (:type IS NULL OR LOWER(b.type) = LOWER(CAST(:type AS string)))
              AND (:color IS NULL OR LOWER(b.color) = LOWER(CAST(:color AS string)))
            """)
    Page<Bike> searchPage(@Param("type") String type,
                          @Param("color") String color,
                          Pageable pageable);

    /**
     * Читання SQL-view {@code bikes_discounted} НАТИВНИМ запитом
     * ({@code nativeQuery = true} — це справжній SQL, а не JPQL). View створює
     * міграція Liquibase; оскільки він віддає всі стовпці таблиці {@code bikes},
     * Hibernate збирає з рядків звичайні сутності {@link Bike}.
     */
    @Query(value = "SELECT * FROM bikes_discounted ORDER BY price", nativeQuery = true)
    List<Bike> findDiscountedView();

    /**
     * Той самий view сторінками. Для нативного запиту Spring Data не вміє сам
     * побудувати підрахунок, тому {@code countQuery} обов'язковий.
     */
    @Query(value = "SELECT * FROM bikes_discounted",
            countQuery = "SELECT count(*) FROM bikes_discounted",
            nativeQuery = true)
    Page<Bike> findDiscountedViewPage(Pageable pageable);

    /**
     * ГЗ 2/4: ПОВНОТЕКСТОВИЙ пошук. JPQL операторів FTS не має, тому запит нативний.
     *
     * {@code plainto_tsquery} нормалізує введений текст і ставить між словами логічне
     * «І»; оператор {@code @@} перевіряє, чи документ ({@code search_vector}) відповідає
     * запиту; {@code ts_rank} рахує релевантність, за якою й сортуємо. Другий критерій
     * сортування — {@code id}: щоб однаково релевантні записи йшли в сталому порядку.
     */
    @Query(value = """
            SELECT * FROM bikes
            WHERE search_vector @@ plainto_tsquery('simple', :q)
            ORDER BY ts_rank(search_vector, plainto_tsquery('simple', :q)) DESC, id
            """, nativeQuery = true)
    List<Bike> searchFullText(@Param("q") String q);

    /**
     * Виклик збереженої функції {@code count_bikes_by_type(text)}, створеної
     * міграцією: рахує рядки сама БД, сюди приходить готове число.
     */
    @Query(value = "SELECT count_bikes_by_type(:type)", nativeQuery = true)
    long countByTypeFunction(@Param("type") String type);

    /** Межа «знижки» для view — тримається поруч із запитом, що її використовує. */
    BigDecimal DISCOUNT_PRICE_LIMIT = new BigDecimal("20000.00");
}
