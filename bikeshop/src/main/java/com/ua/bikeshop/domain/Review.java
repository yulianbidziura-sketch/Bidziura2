package com.ua.bikeshop.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Відгук покупця про велосипед (ГЗ 2/4).
 *
 * ШАР: domain. Зберігається не в PostgreSQL, а в **MongoDB** — у колекції
 * {@code reviews}. Чому саме так: велосипед має сталий набір полів (модель,
 * ціна, розмір рами) і добре лягає в таблицю з фіксованою схемою, а відгук —
 * вільна форма, яка з часом обростає полями (фото, відповідь магазину, позначки
 * «корисно»). У реляційній базі кожне таке поле — міграція; у документній
 * колекції документи можуть відрізнятися між собою.
 *
 * {@code bikeId} — зв'язок із велосипедом у PostgreSQL, але це **НЕ зовнішній
 * ключ**: бази різні й нічого одна про одну не знають. Те, що велосипед існує,
 * перевіряє сам застосунок (див. {@code ReviewServiceImpl}) — у цьому й полягає
 * ціна підходу polyglot persistence.
 */
@Document("reviews")
public class Review {

    @Id
    private String id;              // у Mongo ідентифікатор рядковий (ObjectId)

    private Long bikeId;

    private String author;

    /** Оцінка 1..5. */
    private int rating;

    private String text;

    private Instant createdAt;

    public Review() {
    }

    public Review(String id, Long bikeId, String author, int rating, String text, Instant createdAt) {
        this.id = id;
        this.bikeId = bikeId;
        this.author = author;
        this.rating = rating;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
