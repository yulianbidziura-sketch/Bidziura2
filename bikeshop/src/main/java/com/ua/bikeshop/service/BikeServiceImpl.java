package com.ua.bikeshop.service;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.service.mapper.BikeMapper;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import com.ua.bikeshop.web.dto.PageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реалізація {@link BikeService}.
 *
 * ШАР: service (реалізація). Залежить тільки від абстракцій —
 * {@link BikeRepository} (сховище) і {@link BikeMapper} (мапінг), обидві
 * приходять через конструктор (IoC/DI), тому клас легко тестувати окремо.
 *
 * Саме тут живуть правила, яких не має знати ні контролер, ні репозиторій:
 * «порожній фільтр означає всі велосипеди», «при створенні id призначає
 * сервер», «оновлення й видалення неіснуючого — це 404».
 *
 * ПЗ 1/3: фільтрів стало два (тип і колір), і правило «який фільтр
 * застосовувати» лишилось тут, а не в контролері.
 *
 * ПЗ 2/3: клас лишився тим самим при переході з пам'яті на PostgreSQL —
 * додалися лише нові методи (сторінка, view, функція) і межа транзакції.
 *
 * {@code @Transactional(readOnly = true)} на класі відкриває сесію на час
 * методу: OSIV вимкнено, тому ліниві колекції (галерея фото) мають
 * завантажитись саме тут, доки сесія жива. Методи, що змінюють дані,
 * перекривають це звичайним {@code @Transactional} (читання-запис).
 */
@Service
@Transactional(readOnly = true)
public class BikeServiceImpl implements BikeService {

    private final BikeRepository repository;
    private final BikeMapper mapper;

    public BikeServiceImpl(BikeRepository repository, BikeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<BikeCardDto> findAll(String type, String color) {
        String typeFilter = normalize(type);
        String colorFilter = normalize(color);
        List<Bike> bikes = (typeFilter == null && colorFilter == null)
                ? repository.findAll()                       // жодного фільтра — весь перелік
                : repository.search(typeFilter, colorFilter);
        return bikes.stream()
                .map(mapper::toCard)
                .toList();
    }

    /** Порожній або пробільний параметр трактуємо як «фільтр не задано». */
    private static String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    @Override
    public PageDto<BikeCardDto> findPage(String type, String color, Pageable pageable) {
        Page<Bike> page = repository.search(normalize(type), normalize(color), pageable);
        // назовні віддаємо власний вузький PageDto, а не Spring-тип Page цілком
        return new PageDto<>(
                page.getContent().stream().map(mapper::toCard).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    public List<BikeCardDto> findDiscounted() {
        return repository.findDiscounted().stream()
                .map(mapper::toCard)
                .toList();
    }

    @Override
    public PageDto<BikeCardDto> findDiscountedPage(Pageable pageable) {
        // Без ORDER BY порядок рядків view недетермінований — на різних сторінках
        // той самий велосипед міг би з'явитись двічі. Тому якщо клієнт порядку не
        // задав, упорядковуємо за ціною (акції логічно гортати від найдешевших).
        Pageable effective = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("price"));
        Page<Bike> page = repository.findDiscounted(effective);
        return new PageDto<>(
                page.getContent().stream().map(mapper::toCard).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    public long countByType(String type) {
        return repository.countByType(normalize(type));
    }

    @Override
    public List<BikeCardDto> search(String query) {
        String text = normalize(query);
        if (text == null) {
            // порожній рядок пошуку — не привід віддавати весь каталог:
            // для цього є звичайний перелік
            return List.of();
        }
        return repository.searchFullText(text).stream()
                .map(mapper::toCard)
                .toList();
    }

    @Override
    public BikeDetailDto findById(Long id) {
        Bike bike = repository.findById(id)
                .orElseThrow(() -> new BikeNotFoundException(id));
        return mapper.toDetail(bike);
    }

    @Override
    @Transactional
    public BikeDetailDto create(BikeRequest request) {
        Bike bike = mapper.toDomain(request);
        bike.setId(null);                    // id призначить репозиторій
        return mapper.toDetail(repository.save(bike));
    }

    @Override
    @Transactional
    public BikeDetailDto update(Long id, BikeRequest request) {
        // спершу переконуємось, що велосипед існує (інакше 404, а не тихе створення)
        repository.findById(id)
                .orElseThrow(() -> new BikeNotFoundException(id));
        Bike bike = mapper.toDomain(request);
        bike.setId(id);                      // зберігаємо той самий id
        return mapper.toDetail(repository.save(bike));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        boolean removed = repository.deleteById(id);
        if (!removed) {
            throw new BikeNotFoundException(id);
        }
    }
}
