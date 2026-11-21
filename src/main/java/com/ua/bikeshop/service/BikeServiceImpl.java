package com.ua.bikeshop.service;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.exception.BikeNotFoundException;
import com.ua.bikeshop.repository.BikeRepository;
import com.ua.bikeshop.service.mapper.BikeMapper;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Реалізація {@link BikeService}.
 *
 * ШАР: service (реалізація). Залежить тільки від абстракцій —
 * {@link BikeRepository} (сховище) і {@link BikeMapper} (мапінг), обидві
 * приходять через конструктор (IoC/DI), тому клас легко тестувати окремо.
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
    public List<BikeCardDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toCard)
                .toList();
    }

    @Override
    public BikeDetailDto findById(Long id) {
        Bike bike = repository.findById(id)
                .orElseThrow(() -> new BikeNotFoundException(id));
        return mapper.toDetail(bike);
    }
}
