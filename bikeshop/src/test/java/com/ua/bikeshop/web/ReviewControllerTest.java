package com.ua.bikeshop.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.service.ReviewService;
import com.ua.bikeshop.web.dto.ReviewDto;
import com.ua.bikeshop.web.dto.ReviewRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Зріз веб-шару для відгуків (ГЗ 2/4): жодної бази, сервіс замокано —
 * перевіряється сам контролер: вкладений маршрут, коди 200/201/400/404 і те,
 * що невалідне тіло до сервісу не доходить.
 */
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void list_returnsReviewsOfBike() throws Exception {
        when(reviewService.findByBike(1L)).thenReturn(List.of(review()));

        mockMvc.perform(get("/api/bikes/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("Олег"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].bikeId").value(1));
    }

    @Test
    void list_returns404WhenServiceSaysBikeIsMissing() throws Exception {
        when(reviewService.findByBike(999L)).thenThrow(new BikeNotFoundException(999L));

        mockMvc.perform(get("/api/bikes/999/reviews"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void add_returns201WithCreatedReview() throws Exception {
        when(reviewService.add(eq(1L), any(ReviewRequest.class))).thenReturn(review());

        mockMvc.perform(post("/api/bikes/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ReviewRequest("Олег", 5, "Чудовий велосипед"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("65f0c1a2b3c4d5e6f7a8b9c0"));
    }

    @Test
    void add_returns400AndDoesNotCallServiceWhenRatingIsZero() throws Exception {
        mockMvc.perform(post("/api/bikes/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ReviewRequest("Олег", 0, "Текст"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("rating: оцінка не може бути меншою за 1"));

        verify(reviewService, never()).add(any(), any());
    }

    private static ReviewDto review() {
        return new ReviewDto("65f0c1a2b3c4d5e6f7a8b9c0", 1L, "Олег", 5,
                "Чудовий велосипед", Instant.parse("2026-11-28T10:15:30Z"));
    }
}
