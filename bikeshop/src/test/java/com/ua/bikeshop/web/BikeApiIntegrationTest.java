package com.ua.bikeshop.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 */
@SpringBootTest
@AutoConfigureMockMvc
class BikeApiIntegrationTest {

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
