package com.ua.bikeshop.service.mapper;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юніт-тест маппера: картка містить лише поля переліку,
 * деталі — повний набір полів велосипеда.
 */
class BikeMapperTest {

    private final BikeMapper mapper = new BikeMapper();

    @Test
    void toCard_takesOnlyListFields() {
        BikeCardDto card = mapper.toCard(sample());

        assertThat(card.id()).isEqualTo(1L);
        assertThat(card.model()).isEqualTo("Trail 29 Pro");
        assertThat(card.brand()).isEqualTo("Ridex");
        assertThat(card.type()).isEqualTo("Гірський");
        assertThat(card.price()).isEqualByComparingTo("41900.00");
        assertThat(card.imageUrl()).isEqualTo("/images/bike-trail.svg");
    }

    @Test
    void toDetail_copiesAllFields() {
        BikeDetailDto detail = mapper.toDetail(sample());

        assertThat(detail.frameSize()).isEqualTo("L");
        assertThat(detail.wheelSize()).isEqualByComparingTo("29.0");
        assertThat(detail.weightKg()).isEqualByComparingTo("13.40");
        assertThat(detail.year()).isEqualTo(2024);
        assertThat(detail.images()).hasSize(2);
        assertThat(detail.inStock()).isEqualTo(6);
        assertThat(detail.type()).isEqualTo("Гірський");
        assertThat(detail.color()).isEqualTo("Чорний");
    }

    @Test
    void toDomain_buildsBikeWithoutId() {
        BikeRequest request = new BikeRequest("Enduro 29", "Ridex", "Гірський",
                new BigDecimal("52000.00"), "Алюміній", "M", new BigDecimal("29.0"), 12,
                new BigDecimal("14.10"), 2025, "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg"), "Новий велосипед", 2, "Чорний");

        Bike bike = mapper.toDomain(request);

        assertThat(bike.getId()).isNull();          // id призначить сервер
        assertThat(bike.getModel()).isEqualTo("Enduro 29");
        assertThat(bike.getType()).isEqualTo("Гірський");
        assertThat(bike.getPrice()).isEqualByComparingTo("52000.00");
        assertThat(bike.getInStock()).isEqualTo(2);
        assertThat(bike.getColor()).isEqualTo("Чорний");
    }

    @Test
    void toDomain_treatsMissingImagesAsEmptyList() {
        BikeRequest request = new BikeRequest("Enduro 29", "Ridex", "Гірський",
                new BigDecimal("52000.00"), "Алюміній", "M", new BigDecimal("29.0"), 12,
                new BigDecimal("14.10"), 2025, "/images/bike-trail.svg",
                null, "Новий велосипед", 2, "Чорний");

        assertThat(mapper.toDomain(request).getImages()).isEmpty();
    }

    private Bike sample() {
        return new Bike(1L, "Trail 29 Pro", "Ridex", "Гірський", new BigDecimal("41900.00"), "Алюміній", "L",
                new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg", "/images/bike-trail-2.svg"),
                "Гірський велосипед", 6, "Чорний");
    }
}
