package com.ua.bikeshop.service.mapper;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import com.ua.bikeshop.web.dto.BikeRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Перетворення доменного {@link Bike} у DTO.
 *
 * ШАР: service/mapper. Винесено окремо від сервісу (SRP): сервіс відповідає за
 * логіку, маппер — тільки за перетворення форматів. Коли мапінгів побільшає,
 * усі вони будуть в одному місці.
 *
 * ПЗ 2/3: {@code toDetail} тепер МАТЕРІАЛІЗУЄ галерею фото — копіює її у
 * звичайний список. Після переходу на JPA {@code bike.getImages()} — це ліниве
 * проксі: якби воно потрапило в DTO як є, читання з БД сталося б пізніше, під
 * час серіалізації у JSON, коли транзакція вже закрита (OSIV вимкнено), і ми
 * отримали б LazyInitializationException. Копіювання перебирає елементи ще в
 * транзакції — тобто змушує проксі сходити в базу вчасно.
 */
@Component
public class BikeMapper {

    /** Домен → картка для переліку. */
    public BikeCardDto toCard(Bike bike) {
        return new BikeCardDto(
                bike.getId(),
                bike.getModel(),
                bike.getBrand(),
                bike.getType(),
                bike.getPrice(),
                bike.getImageUrl()
        );
    }

    /** Домен → повні дані для сторінки велосипеда. */
    public BikeDetailDto toDetail(Bike bike) {
        return new BikeDetailDto(
                bike.getId(),
                bike.getModel(),
                bike.getBrand(),
                bike.getType(),
                bike.getPrice(),
                bike.getFrameMaterial(),
                bike.getFrameSize(),
                bike.getWheelSize(),
                bike.getGears(),
                bike.getWeightKg(),
                bike.getYear(),
                bike.getImageUrl(),
                bike.getImages() == null ? List.of() : List.copyOf(bike.getImages()),
                bike.getDescription(),
                bike.getInStock(),
                bike.getColor()
        );
    }

    /**
     * Вхідний DTO → доменний об'єкт (для створення й оновлення).
     *
     * id тут навмисно не встановлюється: при створенні його призначить
     * репозиторій, при оновленні — сервіс візьме його зі шляху запиту.
     */
    public Bike toDomain(BikeRequest request) {
        Bike bike = new Bike();
        bike.setModel(request.model());
        bike.setBrand(request.brand());
        bike.setType(request.type());
        bike.setPrice(request.price());
        bike.setFrameMaterial(request.frameMaterial());
        bike.setFrameSize(request.frameSize());
        bike.setWheelSize(request.wheelSize());
        bike.setGears(request.gears());
        bike.setWeightKg(request.weightKg());
        bike.setYear(request.year());
        bike.setImageUrl(request.imageUrl());
        bike.setImages(request.images() == null ? List.of() : request.images());
        bike.setDescription(request.description());
        bike.setInStock(request.inStock());
        bike.setColor(request.color());
        return bike;
    }
}
