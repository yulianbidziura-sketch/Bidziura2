package com.ua.bikeshop.service;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.service.mapper.BikeMapper;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void findAll_withoutFilter_takesAllBikes() {
        when(repository.findAll()).thenReturn(List.of(bike(1L, "Trail 29 Pro"), bike(2L, "Road Carbon 2")));

        List<BikeCardDto> cards = service.findAll(null, null);

        assertThat(cards).hasSize(2);
        assertThat(cards.get(0).model()).isEqualTo("Trail 29 Pro");
        verify(repository, never()).search(any(), any());
    }

    @Test
    void findAll_withBlankFilter_takesAllBikes() {
        when(repository.findAll()).thenReturn(List.of(bike(1L, "Trail 29 Pro")));

        assertThat(service.findAll("   ", "  ")).hasSize(1);
        verify(repository, never()).search(any(), any());
    }

    @Test
    void findAll_withTypeFilter_asksRepositoryWithTypeOnly() {
        when(repository.search("Гірський", null)).thenReturn(List.of(bike(1L, "Trail 29 Pro")));

        List<BikeCardDto> cards = service.findAll("Гірський", null);

        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).type()).isEqualTo("Гірський");
    }

    @Test
    void findAll_withColorFilter_asksRepositoryWithColorOnly() {
        when(repository.search(null, "Чорний")).thenReturn(List.of(bike(1L, "Trail 29 Pro")));

        assertThat(service.findAll(null, "Чорний")).hasSize(1);
        verify(repository).search(null, "Чорний");
        verify(repository, never()).findAll();
    }

    @Test
    void findAll_withBothFilters_passesBothAndTrimsThem() {
        when(repository.search("Гірський", "Чорний")).thenReturn(List.of(bike(1L, "Trail 29 Pro")));

        assertThat(service.findAll("  Гірський ", " Чорний ")).hasSize(1);
        verify(repository).search("Гірський", "Чорний");
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

    @Test
    void create_savesBikeWithoutId() {
        when(repository.save(any(Bike.class))).thenAnswer(call -> {
            Bike saved = call.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        BikeDetailDto created = service.create(request("Enduro 29"));

        ArgumentCaptor<Bike> captor = ArgumentCaptor.forClass(Bike.class);
        verify(repository).save(captor.capture());
        assertThat(created.id()).isEqualTo(7L);
        assertThat(created.model()).isEqualTo("Enduro 29");
        assertThat(captor.getValue().getModel()).isEqualTo("Enduro 29");
    }

    @Test
    void update_keepsSameId() {
        when(repository.findById(1L)).thenReturn(Optional.of(bike(1L, "Trail 29 Pro")));
        when(repository.save(any(Bike.class))).thenAnswer(call -> call.getArgument(0));

        BikeDetailDto updated = service.update(1L, request("Trail 29 Pro 2025"));

        assertThat(updated.id()).isEqualTo(1L);
        assertThat(updated.model()).isEqualTo("Trail 29 Pro 2025");
    }

    @Test
    void update_throwsWhenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, request("Немає такого")))
                .isInstanceOf(BikeNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_removesExistingBike() {
        when(repository.deleteById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(repository.deleteById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(BikeNotFoundException.class);
    }

    private Bike bike(Long id, String model) {
        return new Bike(id, model, "Ridex", "Гірський", new BigDecimal("41900.00"), "Алюміній",
                "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, "Чорний");
    }

    private BikeRequest request(String model) {
        return new BikeRequest(model, "Ridex", "Гірський", new BigDecimal("41900.00"), "Алюміній",
                "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"), "Опис", 6, "Чорний");
    }
}
