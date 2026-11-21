package com.ua.bikeshop.service.mapper;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
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
    }

    private Bike sample() {
        return new Bike(1L, "Trail 29 Pro", "Ridex", new BigDecimal("41900.00"), "Алюміній", "L",
                new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg", "/images/bike-trail-2.svg"),
                "Гірський велосипед", 6);
    }
}
