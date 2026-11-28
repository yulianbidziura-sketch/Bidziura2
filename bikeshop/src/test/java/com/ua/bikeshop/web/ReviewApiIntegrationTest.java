package com.ua.bikeshop.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ua.bikeshop.AbstractIntegrationTest;
import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.repository.mongo.ReviewRepository;
import com.ua.bikeshop.web.dto.ReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Наскрізний тест відгуків (ГЗ 2/4) — одразу проти ДВОХ справжніх баз:
 * велосипеди в PostgreSQL, відгуки в MongoDB.
 *
 * Тести автономні: @BeforeEach чистить ОБИДВА сховища, кожен тест сам створює
 * велосипед і свої відгуки.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReviewApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BikeRepository bikes;

    @Autowired
    private ReviewRepository reviews;

    @BeforeEach
    void clearBothStores() {
        reviews.deleteAll();
        bikes.deleteAll();
    }

    @Test
    void addReview_returns201AndStoresItInMongo() throws Exception {
        long bikeId = givenBike();

        mockMvc.perform(post("/api/bikes/" + bikeId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReviewRequest("Олег", 5, "Їздив гірськими стежками — тримає чудово."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())        // id призначила Mongo
                .andExpect(jsonPath("$.bikeId").value(bikeId))
                .andExpect(jsonPath("$.author").value("Олег"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.createdAt").isNotEmpty()); // час ставить сервер

        assertThat(reviews.countByBikeId(bikeId)).isEqualTo(1);
    }

    @Test
    void listReviews_returnsNewestFirst() throws Exception {
        long bikeId = givenBike();
        givenReview(bikeId, "Олег", 5, "Перший відгук");
        givenReview(bikeId, "Ірина", 4, "Другий відгук");
        givenReview(bikeId, "Петро", 3, "Третій відгук");

        mockMvc.perform(get("/api/bikes/" + bikeId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].text").value("Третій відгук"))
                .andExpect(jsonPath("$[2].text").value("Перший відгук"));
    }

    @Test
    void reviewsOfDifferentBikesDoNotMix() throws Exception {
        long first = givenBike();
        long second = givenBike();
        givenReview(first, "Олег", 5, "Відгук про перший");
        givenReview(second, "Ірина", 4, "Відгук про другий");

        mockMvc.perform(get("/api/bikes/" + first + "/reviews"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Відгук про перший"));
    }

    @Test
    void addReview_returns404WhenBikeDoesNotExist() throws Exception {
        // зв'язок між сховищами не підкріплений зовнішнім ключем — Mongo прийняла б
        // будь-який bikeId, тому існування велосипеда перевіряє сам застосунок
        mockMvc.perform(post("/api/bikes/999999/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReviewRequest("Олег", 5, "Відгук у нікуди"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertThat(reviews.count()).isZero();      // відгук не створився
    }

    @Test
    void listReviews_returns404WhenBikeDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/bikes/999999/reviews"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addReview_returns400WhenRatingIsOutOfRange() throws Exception {
        long bikeId = givenBike();

        mockMvc.perform(post("/api/bikes/" + bikeId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReviewRequest("Олег", 7, "Оцінка поза шкалою"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details[0]").value("rating: оцінка не може бути більшою за 5"));

        assertThat(reviews.count()).isZero();
    }

    @Test
    void addReview_returns400WhenAuthorAndTextAreBlank() throws Exception {
        long bikeId = givenBike();

        mockMvc.perform(post("/api/bikes/" + bikeId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReviewRequest("  ", 4, ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(2))
                .andExpect(jsonPath("$.details[0]").value("author: ім'я автора обов'язкове"))
                .andExpect(jsonPath("$.details[1]").value("text: текст відгуку обов'язковий"));
    }

    private long givenBike() {
        Bike bike = new Bike(null, "Trail 29 Pro", "Ridex", "Гірський", new BigDecimal("41900.00"),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, "Чорний");
        return bikes.save(bike).getId();
    }

    private void givenReview(long bikeId, String author, int rating, String text) throws Exception {
        mockMvc.perform(post("/api/bikes/" + bikeId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ReviewRequest(author, rating, text))))
                .andExpect(status().isCreated());
    }

    private String json(ReviewRequest request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }
}
