package com.ua.bikeshop.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Доказ інверсії залежностей (ПЗ 2/3).
 *
 * Той самий застосунок під профілем {@code inmemory}: бази даних немає взагалі
 * (автоконфігурацію JPA, джерела даних і Liquibase вимкнено в
 * application-inmemory.yaml), сховищем працює стара реалізація в пам'яті, дані
 * кладе BikeSeeder. Контролер, сервіс, маппер і DTO — ті самі класи, жоден із
 * них не знає, що сховище підмінили.
 *
 * Docker для цього тесту не потрібен.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("inmemory")
class BikeApiInMemoryProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sameApiWorksOnInMemoryStorage() throws Exception {
        mockMvc.perform(get("/api/bikes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));   // п'ять велосипедів сидера
    }

    @Test
    void paginationWorksOnInMemoryStorageToo() throws Exception {
        mockMvc.perform(get("/api/bikes/page").param("size", "2").param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].model").value("Road Carbon 2"))  // найдорожчий
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void discountedAndCountWorkWithoutViewAndFunction() throws Exception {
        // у пам'яті те саме рахує Java, у PostgreSQL — SQL-view і збережена функція
        mockMvc.perform(get("/api/bikes/discounted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));   // Junior 24 і City Lite 7

        mockMvc.perform(get("/api/bikes/count").param("type", "Гірський"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
}
