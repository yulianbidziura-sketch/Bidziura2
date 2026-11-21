package com.ua.bikeshop.service;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.service.mapper.BikeMapper;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import org.springframework.stereotype.Service;

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
 */
@Service
public class BikeServiceImpl implements BikeService {

    private final BikeRepository repository;
    private final BikeMapper mapper;

    public BikeServiceImpl(BikeRepository repository, BikeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<BikeCardDto> findAll(String type) {
        List<Bike> bikes = (type == null || type.isBlank())
                ? repository.findAll()
                : repository.findByType(type);
        return bikes.stream()
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
    public BikeDetailDto create(BikeRequest request) {
        Bike bike = mapper.toDomain(request);
        bike.setId(null);                    // id призначить репозиторій
        return mapper.toDetail(repository.save(bike));
    }

    @Override
    public BikeDetailDto update(Long id, BikeRequest request) {
        // спершу переконуємось, що велосипед існує (інакше 404, а не тихе створення)
        repository.findById(id)
                .orElseThrow(() -> new BikeNotFoundException(id));
        Bike bike = mapper.toDomain(request);
        bike.setId(id);                      // зберігаємо той самий id
        return mapper.toDetail(repository.save(bike));
    }

    @Override
    public void delete(Long id) {
        boolean removed = repository.deleteById(id);
        if (!removed) {
            throw new BikeNotFoundException(id);
        }
    }
}
