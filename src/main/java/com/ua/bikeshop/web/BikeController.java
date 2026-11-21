package com.ua.bikeshop.web;

import com.ua.bikeshop.service.BikeService;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API велосипедів.
 *
 * ШАР: web. Залежить лише від {@link BikeService} (інтерфейсу) — DIP.
 * Відповідає тільки за HTTP: прийняти запит і повернути DTO; жодної логіки.
 *
 * Заняття 1/1 — два ендпоінти читання:
 *   GET /api/bikes        → перелік карток
 *   GET /api/bikes/{id}   → один велосипед з деталями (404, якщо немає)
 */
@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @GetMapping
    public List<BikeCardDto> list() {
        return bikeService.findAll();
    }

    @GetMapping("/{id}")
    public BikeDetailDto getOne(@PathVariable Long id) {
        return bikeService.findById(id);
    }
}
