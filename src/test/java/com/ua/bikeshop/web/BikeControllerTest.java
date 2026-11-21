package com.ua.bikeshop.web;

import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.service.BikeService;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест веб-шару: піднімається лише контролер (@WebMvcTest), сервіс підставлено
 * моком. Перевіряємо маршрути, статуси й структуру JSON.
 */
@WebMvcTest(BikeController.class)
class BikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BikeService bikeService;

    @Test
    void list_returnsBikeCards() throws Exception {
        when(bikeService.findAll()).thenReturn(List.of(
                new BikeCardDto(1L, "Trail 29 Pro", "Ridex",
                        new BigDecimal("41900.00"), "/images/bike-trail.svg")
        ));

        mockMvc.perform(get("/api/bikes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].model").value("Trail 29 Pro"))
                .andExpect(jsonPath("$[0].brand").value("Ridex"))
                .andExpect(jsonPath("$[0].imageUrl").value("/images/bike-trail.svg"));
    }

    @Test
    void getOne_returnsBikeDetail() throws Exception {
        when(bikeService.findById(1L)).thenReturn(new BikeDetailDto(
                1L, "Trail 29 Pro", "Ridex", new BigDecimal("41900.00"),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg", "/images/bike-trail-2.svg"),
                "Гірський велосипед", 6));

        mockMvc.perform(get("/api/bikes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.frameMaterial").value("Алюміній"))
                .andExpect(jsonPath("$.gears").value(12))
                .andExpect(jsonPath("$.images.length()").value(2));
    }

    @Test
    void getOne_returns404WhenMissing() throws Exception {
        when(bikeService.findById(999L)).thenThrow(new BikeNotFoundException(999L));

        mockMvc.perform(get("/api/bikes/999"))
                .andExpect(status().isNotFound());
    }
}
