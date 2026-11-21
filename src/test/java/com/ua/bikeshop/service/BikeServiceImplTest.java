package com.ua.bikeshop.service;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.service.mapper.BikeMapper;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Юніт-тест сервісу: репозиторій замокано, маппер справжній —
 * перевіряється саме логіка сервісу, без Spring-контексту.
 */
class BikeServiceImplTest {

    private BikeRepository repository;
    private BikeService service;

    @BeforeEach
    void setUp() {
        repository = mock(BikeRepository.class);
        service = new BikeServiceImpl(repository, new BikeMapper());
    }

    @Test
    void findAll_mapsEveryBikeToCard() {
        when(repository.findAll()).thenReturn(List.of(bike(1L, "Trail 29 Pro"), bike(2L, "Road Carbon 2")));

        List<BikeCardDto> cards = service.findAll();

        assertThat(cards).hasSize(2);
        assertThat(cards.get(0).model()).isEqualTo("Trail 29 Pro");
        assertThat(cards.get(1).model()).isEqualTo("Road Carbon 2");
    }

    @Test
    void findById_returnsDetail() {
        when(repository.findById(1L)).thenReturn(Optional.of(bike(1L, "Trail 29 Pro")));

        BikeDetailDto detail = service.findById(1L);

        assertThat(detail.id()).isEqualTo(1L);
        assertThat(detail.frameMaterial()).isEqualTo("Алюміній");
        assertThat(detail.gears()).isEqualTo(12);
    }

    @Test
    void findById_throwsWhenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(BikeNotFoundException.class)
                .hasMessageContaining("999");
    }

    private Bike bike(Long id, String model) {
        return new Bike(id, model, "Ridex", new BigDecimal("41900.00"), "Алюміній", "L",
                new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6);
    }
}
