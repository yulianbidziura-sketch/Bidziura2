package com.ua.bikeshop.web;

import com.ua.bikeshop.service.ReviewService;
import com.ua.bikeshop.web.dto.ReviewDto;
import com.ua.bikeshop.web.dto.ReviewRequest;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API відгуків (ГЗ 2/4).
 *
 * ШАР: web. Відгуки — ПІД-РЕСУРС велосипеда, тому й шлях вкладений:
 *   GET  /api/bikes/{id}/reviews  → відгуки про велосипед (найновіші спершу)
 *   POST /api/bikes/{id}/reviews  → додати відгук (201; 404, якщо велосипеда немає)
 *
 * Окремий контролер, а не метод у {@link BikeController}, бо це інший ресурс
 * з іншим сховищем — і єдина відповідальність класу від цього тільки виграє.
 *
 * Помилки йдуть тим самим шляхом, що й усюди: {@code BikeNotFoundException} →
 * 404, порушення {@code @Valid} (оцінка поза 1..5, порожній текст) → 400 — усе
 * через {@link GlobalExceptionHandler} з єдиним форматом тіла.
 */
@RestController
@RequestMapping("/api/bikes/{bikeId}/reviews")
@Profile("!inmemory")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewDto> list(@PathVariable Long bikeId) {
        return reviewService.findByBike(bikeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto add(@PathVariable Long bikeId, @Valid @RequestBody ReviewRequest request) {
        return reviewService.add(bikeId, request);
    }
}
