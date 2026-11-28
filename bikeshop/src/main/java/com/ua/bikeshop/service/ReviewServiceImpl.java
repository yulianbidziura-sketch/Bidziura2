package com.ua.bikeshop.service;

import com.ua.bikeshop.domain.Review;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.repository.mongo.ReviewRepository;
import com.ua.bikeshop.web.dto.ReviewDto;
import com.ua.bikeshop.web.dto.ReviewRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Реалізація {@link ReviewService} (ГЗ 2/4).
 *
 * ШАР: service. Тут сходяться ДВА сховища: велосипеди в PostgreSQL
 * ({@link BikeRepository}) і відгуки в MongoDB ({@link ReviewRepository}).
 *
 * Головне місце всього заняття — перевірка перед додаванням відгуку. Зв'язок
 * між базами не підкріплений зовнішнім ключем: Mongo не знає про таблицю
 * {@code bikes} і прийняла б відгук із будь-яким {@code bikeId}. Тому
 * узгодженість тримає застосунок: немає велосипеда — {@code BikeNotFoundException}
 * і 404, а відгук не створюється.
 *
 * Транзакції тут не потрібні: кожна операція стосується ОДНОГО документа, а
 * запис одного документа в MongoDB атомарний сам собою.
 */
@Service
@Profile("!inmemory")
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviews;
    private final BikeRepository bikes;

    public ReviewServiceImpl(ReviewRepository reviews, BikeRepository bikes) {
        this.reviews = reviews;
        this.bikes = bikes;
    }

    @Override
    public List<ReviewDto> findByBike(Long bikeId) {
        requireBike(bikeId);
        return reviews.findByBikeIdOrderByCreatedAtDescIdDesc(bikeId).stream()
                .map(ReviewServiceImpl::toDto)
                .toList();
    }

    @Override
    public ReviewDto add(Long bikeId, ReviewRequest request) {
        requireBike(bikeId);
        Review review = new Review(
                null,                       // id призначить Mongo
                bikeId,
                request.author().trim(),
                request.rating(),
                request.text().trim(),
                Instant.now());             // час ставить сервер, а не клієнт
        return toDto(reviews.save(review));
    }

    /** Відгук можна читати й писати лише для велосипеда, який справді існує. */
    private void requireBike(Long bikeId) {
        if (bikes.findById(bikeId).isEmpty()) {
            throw new BikeNotFoundException(bikeId);
        }
    }

    private static ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getBikeId(),
                review.getAuthor(),
                review.getRating(),
                review.getText(),
                review.getCreatedAt());
    }
}
