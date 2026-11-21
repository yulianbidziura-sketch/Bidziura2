package com.ua.bikeshop.service.mapper;

import com.ua.bikeshop.domain.Bike;
import com.ua.bikeshop.web.dto.BikeCardDto;
import com.ua.bikeshop.web.dto.BikeDetailDto;
import org.springframework.stereotype.Component;

/**
 * Перетворення доменного {@link Bike} у DTO.
 *
 * ШАР: service/mapper. Винесено окремо від сервісу (SRP): сервіс відповідає за
 * логіку, маппер — тільки за перетворення форматів. Коли мапінгів побільшає,
 * усі вони будуть в одному місці.
 */
@Component
public class BikeMapper {

    /** Домен → картка для переліку. */
    public BikeCardDto toCard(Bike bike) {
        return new BikeCardDto(
                bike.getId(),
                bike.getModel(),
                bike.getBrand(),
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
                bike.getPrice(),
                bike.getFrameMaterial(),
                bike.getFrameSize(),
                bike.getWheelSize(),
                bike.getGears(),
                bike.getWeightKg(),
                bike.getYear(),
                bike.getImageUrl(),
                bike.getImages(),
                bike.getDescription(),
                bike.getInStock()
        );
    }
}
