package com.ua.bikeshop.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Доменна модель велосипеда.
 *
 * ШАР: domain. Навмисно БЕЗ анотацій Spring і без залежностей на фреймворк:
 * це серце застосунку, яке не має знати ні про HTTP, ні про спосіб зберігання.
 * Саме тому верхні шари (service, web) залежать від домену, а не навпаки — DIP.
 *
 * Заняття 1/1: дані тримаються в пам'яті, поля підібрані так, щоб їх вистачило
 * і на картку в переліку (модель, бренд, ціна, фото), і на сторінку велосипеда
 * (рама, колеса, швидкості, вага, опис, галерея).
 */
public class Bike {

    private Long id;

    /** Модель, напр. «Trail 29 Pro». */
    private String model;

    /** Виробник, напр. «Pride». */
    private String brand;

    private BigDecimal price;

    /** Матеріал рами: алюміній, карбон, сталь. */
    private String frameMaterial;

    /** Розмір рами: S, M, L, XL. */
    private String frameSize;

    /** Діаметр коліс у дюймах, напр. 29.0. */
    private BigDecimal wheelSize;

    /** Кількість швидкостей трансмісії. */
    private Integer gears;

    /** Вага велосипеда в кілограмах. */
    private BigDecimal weightKg;

    /** Рік модельного ряду. */
    private Integer year;

    /** Головне фото (показується на картці в переліку). */
    private String imageUrl;

    /** Усі фото (галерея на сторінці велосипеда). */
    private List<String> images;

    private String description;

    /** Залишок на складі, шт. */
    private Integer inStock;

    public Bike() {
    }

    public Bike(Long id, String model, String brand, BigDecimal price, String frameMaterial,
                String frameSize, BigDecimal wheelSize, Integer gears, BigDecimal weightKg,
                Integer year, String imageUrl, List<String> images, String description,
                Integer inStock) {
        this.id = id;
        this.model = model;
        this.brand = brand;
        this.price = price;
        this.frameMaterial = frameMaterial;
        this.frameSize = frameSize;
        this.wheelSize = wheelSize;
        this.gears = gears;
        this.weightKg = weightKg;
        this.year = year;
        this.imageUrl = imageUrl;
        this.images = images;
        this.description = description;
        this.inStock = inStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getFrameMaterial() {
        return frameMaterial;
    }

    public void setFrameMaterial(String frameMaterial) {
        this.frameMaterial = frameMaterial;
    }

    public String getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(String frameSize) {
        this.frameSize = frameSize;
    }

    public BigDecimal getWheelSize() {
        return wheelSize;
    }

    public void setWheelSize(BigDecimal wheelSize) {
        this.wheelSize = wheelSize;
    }

    public Integer getGears() {
        return gears;
    }

    public void setGears(Integer gears) {
        this.gears = gears;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getInStock() {
        return inStock;
    }

    public void setInStock(Integer inStock) {
        this.inStock = inStock;
    }
}
