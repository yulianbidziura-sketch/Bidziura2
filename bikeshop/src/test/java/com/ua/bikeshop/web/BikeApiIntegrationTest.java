package com.ua.bikeshop.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ua.bikeshop.AbstractIntegrationTest;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.web.dto.BikeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Наскрізний (end-to-end) тест API — ГЗ 1/4.
 *
 * На відміну від зрізу {@code @WebMvcTest}, тут {@code @SpringBootTest} піднімає
 * ВЕСЬ контекст із реальними бінами: запит проходить контролер → сервіс → маппер
 * → сховище. Нічого не мокається. {@code @AutoConfigureMockMvc} дає MockMvc,
 * тому справжній Tomcat не потрібен.
 *
 * Тести АВТОНОМНІ (незалежні один від одного й від порядку виконання):
 *   1) {@code @BeforeEach} очищає сховище — чистий старт;
 *   2) кожен тест САМ створює потрібні йому дані через API і бере звідти
 *      справжній id (ніяких припущень «id = 1»);
 *   3) перевіряється лише те, що тест створив сам — ні стартові дані сидера,
 *      ні чужі записи не враховуються.
 *
 * ПЗ 2/3: під тестом тепер СПРАВЖНІЙ PostgreSQL із контейнера
 * ({@link AbstractIntegrationTest}), а схему в ньому створюють ті самі міграції
 * Liquibase, що й у робочому застосунку. Тобто перевіряється вже не лише код,
 * а й зв'язка «код + схема + запити».
 */
@SpringBootTest
@AutoConfigureMockMvc
class BikeApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BikeRepository repository;

    @BeforeEach
    void clearState() {
        repository.deleteAll();     // стартові дані BikeSeeder у тестах не використовуються
    }

    @Test
    void list_returnsExactlyWhatThisTestCreated() throws Exception {
        givenBike("Trail 29 Pro", "Гірський", "Чорний");
        givenBike("Road Carbon 2", "Шосейний", "Червоний");

        mockMvc.perform(get("/api/bikes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void list_filtersByTypeThroughAllLayers() throws Exception {
        givenBike("Trail 29 Pro", "Гірський", "Чорний");
        givenBike("Road Carbon 2", "Шосейний", "Червоний");

        mockMvc.perform(get("/api/bikes").param("type", "Гірський"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Trail 29 Pro"));
    }

    @Test
    void list_filtersByColorAndByBothCriteria() throws Exception {
        givenBike("Trail 29 Pro", "Гірський", "Чорний");
        givenBike("City Lite 7", "Міський", "Чорний");

        mockMvc.perform(get("/api/bikes").param("color", "Чорний"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/bikes")
                        .param("type", "Міський")
                        .param("color", "Чорний"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("City Lite 7"));
    }

    @Test
    void getOne_returnsDetailOfCreatedBike() throws Exception {
        long id = givenBike("Gravel One", "Гравійний", "Пісочний");

        mockMvc.perform(get("/api/bikes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.model").value("Gravel One"))
                .andExpect(jsonPath("$.color").value("Пісочний"))
                .andExpect(jsonPath("$.images.length()").value(1));
    }

    @Test
    void getOne_returns404WithStructuredBody() throws Exception {
        mockMvc.perform(get("/api/bikes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Велосипед з id=999 не знайдено"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void create_storesBikeAndAssignsId() throws Exception {
        long id = givenBike("Enduro 29", "Гірський", "Зелений");

        mockMvc.perform(get("/api/bikes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Enduro 29"));
    }

    @Test
    void update_changesFieldsAndKeepsSameId() throws Exception {
        long id = givenBike("Trail 29 Pro", "Гірський", "Чорний");

        mockMvc.perform(put("/api/bikes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request("Trail 29 Pro 2025", "Гірський", "Синій"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.model").value("Trail 29 Pro 2025"))
                .andExpect(jsonPath("$.color").value("Синій"));
    }

    @Test
    void update_returns404ForMissingId() throws Exception {
        mockMvc.perform(put("/api/bikes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request("Немає такого", "Гірський", "Чорний"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void delete_removesBikeAndSecondAttemptGives404() throws Exception {
        long id = givenBike("Junior 24", "Підлітковий", "Синій");

        mockMvc.perform(delete("/api/bikes/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/bikes/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/api/bikes"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void list_returnsEmptyArrayWhenFilterMatchesNothing() throws Exception {
        givenBike("Trail 29 Pro", "Гірський", "Чорний");

        mockMvc.perform(get("/api/bikes").param("type", "Шосейний"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));   // порожня колекція — не помилка
    }

    @Test
    void update_returns400AndKeepsStoredBikeUntouched() throws Exception {
        long id = givenBike("Trail 29 Pro", "Гірський", "Чорний");

        BikeRequest invalid = new BikeRequest("Trail 29 Pro", "Ridex", "   ",
                new BigDecimal("41900.00"), "Алюміній", "L", new BigDecimal("29.0"), 12,
                new BigDecimal("13.40"), 2024, "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg"), "Опис", 6, "Чорний");

        mockMvc.perform(put("/api/bikes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("type: тип велосипеда обов'язковий"));

        // невалідний запит не дійшов до сховища — запис лишився колишнім
        mockMvc.perform(get("/api/bikes/" + id))
                .andExpect(jsonPath("$.type").value("Гірський"));
    }

    @Test
    void create_returns400WithDetailsWhenBodyIsInvalid() throws Exception {
        BikeRequest invalid = new BikeRequest("", "Ridex", "Гірський", new BigDecimal("-100.00"),
                "Алюміній", "M", new BigDecimal("29.0"), 12, new BigDecimal("14.10"), 2025,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 2, "Чорний");

        mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(2))
                .andExpect(jsonPath("$.details[0]").value("model: модель обов'язкова"))
                .andExpect(jsonPath("$.details[1]").value("price: ціна має бути додатною"));
    }

    // --- ПЗ 2/3: пагінація, сортування, SQL-view і функція крізь усі шари ---

    @Test
    void page_returnsSliceWithMetadata() throws Exception {
        for (int i = 1; i <= 12; i++) {
            givenBike("Модель " + i, "Гірський", "Чорний");
        }

        mockMvc.perform(get("/api/bikes/page").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void page_secondPageContainsDifferentBikes() throws Exception {
        for (int i = 1; i <= 6; i++) {
            givenBike("Модель " + i, "Гірський", "Чорний");
        }

        String first = contentModels(0, 3);
        String second = contentModels(1, 3);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void page_sortsByPriceDescending() throws Exception {
        givenBike("Дешевий", "Гірський", "Чорний", "10000.00");
        givenBike("Дорогий", "Гірський", "Чорний", "90000.00");
        givenBike("Середній", "Гірський", "Чорний", "50000.00");

        mockMvc.perform(get("/api/bikes/page").param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].model").value("Дорогий"))
                .andExpect(jsonPath("$.content[1].model").value("Середній"))
                .andExpect(jsonPath("$.content[2].model").value("Дешевий"));
    }

    @Test
    void page_combinesFilterWithPagination() throws Exception {
        givenBike("Trail 29 Pro", "Гірський", "Чорний");
        givenBike("Trail 27 Sport", "Гірський", "Сірий");
        givenBike("Road Carbon 2", "Шосейний", "Чорний");

        mockMvc.perform(get("/api/bikes/page").param("type", "Гірський").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));   // рахує лише відфільтровані
    }

    @Test
    void discounted_readsSqlView() throws Exception {
        givenBike("Junior 24", "Підлітковий", "Синій", "9800.00");
        givenBike("Road Carbon 2", "Шосейний", "Чорний", "86500.00");

        mockMvc.perform(get("/api/bikes/discounted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Junior 24"));
    }

    @Test
    void discountedPage_paginatesOverView() throws Exception {
        for (int i = 1; i <= 5; i++) {
            givenBike("Дешевий " + i, "Міський", "Білий", "12000.00");
        }

        mockMvc.perform(get("/api/bikes/discounted/page").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void count_isCalculatedByStoredFunction() throws Exception {
        givenBike("Trail 29 Pro", "Гірський", "Чорний");
        givenBike("Trail 27 Sport", "Гірський", "Сірий");
        givenBike("Road Carbon 2", "Шосейний", "Чорний");

        mockMvc.perform(get("/api/bikes/count").param("type", "Гірський"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void detail_stillReturnsGalleryAfterSwitchToDatabase() throws Exception {
        long id = givenBike("Trail 29 Pro", "Гірський", "Чорний");

        // галерея — лінива колекція; якби її не матеріалізували в транзакції,
        // тут був би LazyInitializationException замість відповіді
        mockMvc.perform(get("/api/bikes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(1))
                .andExpect(jsonPath("$.images[0]").value("/images/bike-trail.svg"));
    }

    // --- ГЗ 2/4: повнотекстовий пошук ---

    @Test
    void search_findsByWholeWordAnywhereInText() throws Exception {
        givenDescribedBike("Trail 29 Pro", "Гірський", "Карбонова рама і дискові гальма");
        givenDescribedBike("City Lite 7", "Міський", "Планетарна втулка і багажник");

        // слово з опису, а не з назви — звичайний фільтр так не вміє
        mockMvc.perform(get("/api/bikes/search").param("q", "багажник"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("City Lite 7"));

        // і слово з типу — у search_vector зведено модель, бренд, тип і опис
        mockMvc.perform(get("/api/bikes/search").param("q", "Гірський"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Trail 29 Pro"));
    }

    @Test
    void search_withSeveralWordsNarrowsResult() throws Exception {
        givenDescribedBike("Trail 29 Pro", "Гірський", "Карбонова рама і дискові гальма");
        givenDescribedBike("Rock Line 29", "Гірський", "Алюмінієва рама і дискові гальма");

        // одне слово — обидва; два слова з'єднуються логічним «І» — лише один
        mockMvc.perform(get("/api/bikes/search").param("q", "рама"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/bikes/search").param("q", "карбонова рама"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Trail 29 Pro"));
    }

    @Test
    void search_wordOrderDoesNotMatter() throws Exception {
        givenDescribedBike("Trail 29 Pro", "Гірський", "Карбонова рама і дискові гальма");

        mockMvc.perform(get("/api/bikes/search").param("q", "гальма карбонова"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_doesNotMatchSubstringUnlikeLike() throws Exception {
        givenDescribedBike("Trail 29 Pro", "Гірський", "Карбонова рама і дискові гальма");

        // LIKE '%рам%' знайшов би «рама», повнотекстовий пошук працює зі СЛОВАМИ
        mockMvc.perform(get("/api/bikes/search").param("q", "рам"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_withEmptyQueryReturnsNothing() throws Exception {
        givenDescribedBike("Trail 29 Pro", "Гірський", "Карбонова рама і дискові гальма");

        mockMvc.perform(get("/api/bikes/search").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/bikes/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_seesBikeCreatedThroughApi() throws Exception {
        // тригер перераховує search_vector при вставці — інакше нові записи
        // були б невидимі для пошуку
        givenDescribedBike("Gravel One", "Гравійний", "Кріплення під багажник і флягу");

        mockMvc.perform(get("/api/bikes/search").param("q", "флягу"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Gravel One"));
    }

    /** Створює велосипед із заданим описом — саме за описом і шукаємо. */
    private void givenDescribedBike(String model, String type, String description) throws Exception {
        BikeRequest request = new BikeRequest(model, "Ridex", type, new BigDecimal("41900.00"),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), description, 6, "Чорний");
        mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated());
    }

    /** Повертає моделі зі сторінки одним рядком — щоб порівняти дві сторінки між собою. */
    private String contentModels(int page, int size) throws Exception {
        String body = mockMvc.perform(get("/api/bikes/page")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("content").toString();
    }

    private long givenBike(String model, String type, String color, String price) throws Exception {
        BikeRequest request = new BikeRequest(model, "Ridex", type, new BigDecimal(price),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, color);
        String body = mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    /**
     * Створює велосипед ЧЕРЕЗ API і повертає його справжній id.
     * Саме так тест готує собі дані, не покладаючись ні на сидер, ні на «id = 1».
     */
    private long givenBike(String model, String type, String color) throws Exception {
        String body = mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(model, type, color))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode created = objectMapper.readTree(body);
        return created.get("id").asLong();
    }

    private BikeRequest request(String model, String type, String color) {
        return new BikeRequest(model, "Ridex", type, new BigDecimal("41900.00"), "Алюміній",
                "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, color);
    }

    private String json(BikeRequest request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }
}
