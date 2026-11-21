package com.ua.bikeshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Найпростіша перевірка: контекст Spring піднімається, усі залежності
 * (контролер → сервіс → репозиторій → маппер) знайдені й зв'язані.
 */
@SpringBootTest
class BikeshopApplicationTests {

    @Test
    void contextLoads() {
    }

}
