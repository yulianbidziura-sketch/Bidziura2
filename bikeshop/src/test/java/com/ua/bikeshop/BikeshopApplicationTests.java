package com.ua.bikeshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Найпростіша перевірка: контекст Spring піднімається, усі залежності
 * (контролер → сервіс → репозиторій → маппер) знайдені й зв'язані.
 *
 * ПЗ 2/3: тепер тут перевіряється ще й те, що застосунок узагалі здатний
 * стартувати з базою — Liquibase відпрацював міграції, а Hibernate у режимі
 * validate визнав схему відповідною сутності. Якби міграція розійшлася з
 * анотаціями Bike, цей тест упав би першим.
 */
@SpringBootTest
class BikeshopApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }

}
