package com.ua.bikeshop.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.service.BikeService;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест веб-шару: піднімається лише контролер (@WebMvcTest), сервіс підставлено
 * моком. Перевіряємо маршрути, статуси (200/201/204/404) і структуру JSON.
 */
@WebMvcTest(BikeController.class)
class BikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BikeService bikeService;

    @Test
    void list_returnsBikeCards() throws Exception {
        when(bikeService.findAll(null, null)).thenReturn(List.of(card()));

        mockMvc.perform(get("/api/bikes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].model").value("Trail 29 Pro"))
                .andExpect(jsonPath("$[0].brand").value("Ridex"))
                .andExpect(jsonPath("$[0].imageUrl").value("/images/bike-trail.svg"));
    }

    @Test
    void list_passesTypeFilterToService() throws Exception {
        when(bikeService.findAll("Гірський", null)).thenReturn(List.of(card()));

        mockMvc.perform(get("/api/bikes").param("type", "Гірський"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("Гірський"));

        verify(bikeService).findAll("Гірський", null);
    }

    @Test
    void list_passesColorFilterToService() throws Exception {
        when(bikeService.findAll(null, "Чорний")).thenReturn(List.of(card()));

        mockMvc.perform(get("/api/bikes").param("color", "Чорний"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bikeService).findAll(null, "Чорний");
    }

    @Test
    void list_passesBothFiltersToService() throws Exception {
        when(bikeService.findAll("Гірський", "Чорний")).thenReturn(List.of(card()));

        mockMvc.perform(get("/api/bikes")
                        .param("type", "Гірський")
                        .param("color", "Чорний"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bikeService).findAll("Гірський", "Чорний");
    }

    @Test
    void getOne_returnsBikeDetail() throws Exception {
        when(bikeService.findById(1L)).thenReturn(detail(1L, "Trail 29 Pro"));

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

    @Test
    void create_returns201WithCreatedBike() throws Exception {
        when(bikeService.create(any(BikeRequest.class))).thenReturn(detail(6L, "Enduro 29"));

        mockMvc.perform(post("/api/bikes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Enduro 29"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.model").value("Enduro 29"));
    }

    @Test
    void update_returns200WithUpdatedBike() throws Exception {
        when(bikeService.update(eq(1L), any(BikeRequest.class)))
                .thenReturn(detail(1L, "Trail 29 Pro 2025"));

        mockMvc.perform(put("/api/bikes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Trail 29 Pro 2025"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.model").value("Trail 29 Pro 2025"));
    }

    @Test
    void update_returns404WhenMissing() throws Exception {
        when(bikeService.update(eq(999L), any(BikeRequest.class)))
                .thenThrow(new BikeNotFoundException(999L));

        mockMvc.perform(put("/api/bikes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Немає такого"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/bikes/1"))
                .andExpect(status().isNoContent());

        verify(bikeService).delete(1L);
    }

    @Test
    void delete_returns404WhenMissing() throws Exception {
        doThrow(new BikeNotFoundException(999L)).when(bikeService).delete(999L);

        mockMvc.perform(delete("/api/bikes/999"))
                .andExpect(status().isNotFound());
    }

    private BikeCardDto card() {
        return new BikeCardDto(1L, "Trail 29 Pro", "Ridex", "Гірський",
                new BigDecimal("41900.00"), "/images/bike-trail.svg");
    }

    private BikeDetailDto detail(Long id, String model) {
        return new BikeDetailDto(id, model, "Ridex", "Гірський", new BigDecimal("41900.00"),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg",
                List.of("/images/bike-trail.svg", "/images/bike-trail-2.svg"),
                "Гірський велосипед", 6, "Чорний");
    }

    private BikeRequest request(String model) {
        return new BikeRequest(model, "Ridex", "Гірський", new BigDecimal("41900.00"),
                "Алюміній", "L", new BigDecimal("29.0"), 12, new BigDecimal("13.40"), 2024,
                "/images/bike-trail.svg", List.of("/images/bike-trail.svg"),
                "Гірський велосипед", 6, "Чорний");
    }
}
