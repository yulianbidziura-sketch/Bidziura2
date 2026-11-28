package com.ua.bikeshop.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

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
 *
 * Заняття 1/2: додано поле {@code type} — за ним працює фільтр у переліку
 * (GET /api/bikes?type=...).
 *
 * ПЗ 1/3: додано поле {@code color} — другий фільтр переліку
 * (GET /api/bikes?color=...). Нове поле дописано в кінець конструктора,
 * щоб не переписувати порядок аргументів у всіх місцях виклику.
 *
 * ПЗ 2/3: клас став JPA-сутністю — на ньому з'явились анотації відображення
 * на таблицю {@code bikes}. Це єдина поступка фреймворку в доменному шарі, і
 * вона свідома: інакше довелося б тримати окремий клас-близнюк лише заради
 * мапінгу. Набір полів, конструктор і гетери НЕ змінились — тому сервіс,
 * маппер і контролер лишились такими самими.
 *
 * Самої схеми цей клас не створює: таблиці робить Liquibase, а Hibernate у
 * режимі {@code ddl-auto: validate} лише звіряє її з цими анотаціями.
 */
@Entity
@Table(name = "bikes")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // id видає сама БД (стовпець-ідентичність)
    private Long id;

    /** Модель, напр. «Trail 29 Pro». */
    @Column(nullable = false)
    private String model;

    /** Виробник, напр. «Pride». */
    @Column(nullable = false)
    private String brand;

    /** Тип велосипеда: гірський, шосейний, міський, гравійний, підлітковий. Поле фільтра. */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    /** Матеріал рами: алюміній, карбон, сталь. */
    @Column(name = "frame_material")
    private String frameMaterial;

    /** Розмір рами: S, M, L, XL. */
    @Column(name = "frame_size", length = 16)
    private String frameSize;

    /** Діаметр коліс у дюймах, напр. 29.0. */
    @Column(name = "wheel_size", precision = 4, scale = 1)
    private BigDecimal wheelSize;

    /** Кількість швидкостей трансмісії. */
    private Integer gears;

    /** Вага велосипеда в кілограмах. */
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    /** Рік модельного ряду. */
    // YEAR — слово зі словника SQL, тому колонку названо model_year
    @Column(name = "model_year")
    private Integer year;

    /** Головне фото (показується на картці в переліку). */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Колекція значень: окремої сутності «фото» немає, тому список зберігається
     * у власній таблиці {@code bike_images}, зв'язаній з {@code bikes} зовнішнім
     * ключем {@code bike_id}. Стовпець {@code position} тримає ПОРЯДОК фото —
     * без нього рядки повертались би в довільній послідовності.
     *
     * Завантаження ліниве (типово для @ElementCollection), тому список читається
     * у межах транзакції сервісу — див. BikeServiceImpl і BikeMapper.
     */
    @ElementCollection
    @CollectionTable(name = "bike_images", joinColumns = @JoinColumn(name = "bike_id"))
    @OrderColumn(name = "position")
    @Column(name = "image_url", length = 500)
    /** Усі фото (галерея на сторінці велосипеда). */
    private List<String> images;

    @Column(length = 2000)
    private String description;

    /** Залишок на складі, шт. */
    @Column(name = "in_stock")
    private Integer inStock;

    /** Колір рами, напр. «Чорний». Друге поле фільтра (ПЗ 1/3). */
    private String color;

    public Bike() {
    }

    public Bike(Long id, String model, String brand, String type, BigDecimal price,
                String frameMaterial, String frameSize, BigDecimal wheelSize, Integer gears,
                BigDecimal weightKg, Integer year, String imageUrl, List<String> images,
                String description, Integer inStock, String color) {
        this.id = id;
        this.model = model;
        this.brand = brand;
        this.type = type;
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
        this.color = color;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
