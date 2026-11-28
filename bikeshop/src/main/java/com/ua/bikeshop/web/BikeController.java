package com.ua.bikeshop.web;

import com.ua.bikeshop.service.BikeService;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import com.ua.bikeshop.web.dto.PageDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API велосипедів.
 *
 * ШАР: web. Залежить лише від {@link BikeService} (інтерфейсу) — DIP.
 * Відповідає тільки за HTTP: прийняти запит і повернути DTO; жодної логіки.
 *
 * Заняття 1/2 — повний CRUD у пам'яті:
 *   GET    /api/bikes              → перелік карток (опц. ?type=... і/або ?color=...)
 *   GET    /api/bikes/{id}         → один велосипед з деталями
 *   POST   /api/bikes              → створити велосипед   (201 Created)
 *   PUT    /api/bikes/{id}         → оновити велосипед    (200 OK / 404)
 *   DELETE /api/bikes/{id}         → видалити велосипед   (204 No Content / 404)
 *
 * ГЗ 1/4: тіло запиту позначено {@code @Valid} — невалідні дані не доходять до
 * сервісу, а перетворюються на 400 у {@link GlobalExceptionHandler}.
 *
 * ПЗ 2/3 — чотири нові ендпоінти читання:
 *   GET /api/bikes/page            → сторінка карток (?page=&size=&sort=, фільтри ті самі)
 *   GET /api/bikes/discounted      → велосипеди зі знижкою (SQL-view у базі)
 *   GET /api/bikes/discounted/page → те саме сторінками
 *   GET /api/bikes/count?type=...  → скільки таких (рахує функція в базі)
 *
 * Старий {@code GET /api/bikes} лишено без змін — додавати новий ендпоінт
 * безпечніше, ніж ламати контракт, на який уже міг розраховувати клієнт.
 */
@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @GetMapping
    public List<BikeCardDto> list(@RequestParam(required = false) String type,
                                  @RequestParam(required = false) String color) {
        return bikeService.findAll(type, color);
    }

    /**
     * Сторінковий перелік. Контролер лише ОГОЛОШУЄ параметр {@link Pageable} —
     * збирає його зі {@code ?page=&size=&sort=} сам Spring Data; розбирати
     * рядок запиту руками не треба.
     */
    @GetMapping("/page")
    public PageDto<BikeCardDto> page(@RequestParam(required = false) String type,
                                     @RequestParam(required = false) String color,
                                     Pageable pageable) {
        return bikeService.findPage(type, color, pageable);
    }

    @GetMapping("/discounted")
    public List<BikeCardDto> discounted() {
        return bikeService.findDiscounted();
    }

    @GetMapping("/discounted/page")
    public PageDto<BikeCardDto> discountedPage(Pageable pageable) {
        return bikeService.findDiscountedPage(pageable);
    }

    @GetMapping("/count")
    public long count(@RequestParam(required = false) String type) {
        return bikeService.countByType(type);
    }

    /**
     * Оголошено ПІСЛЯ літеральних шляхів свідомо: {@code /page}, {@code /count}
     * і {@code /discounted} мали б інакше шанс потрапити сюди як {@code {id}}.
     */
    @GetMapping("/{id}")
    public BikeDetailDto getOne(@PathVariable Long id) {
        return bikeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BikeDetailDto create(@Valid @RequestBody BikeRequest request) {
        return bikeService.create(request);
    }

    @PutMapping("/{id}")
    public BikeDetailDto update(@PathVariable Long id, @Valid @RequestBody BikeRequest request) {
        return bikeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bikeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
