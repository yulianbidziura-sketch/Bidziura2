# Приклади запитів — VeloUA (заняття 1/2, повний CRUD)

Бек має бути запущений: `./mvnw spring-boot:run` (порт 8080, JDK 21).

На 1/2 доступні всі шість операцій: перелік, фільтр за типом, один велосипед,
створення, оновлення, видалення.

---

## 1. Перелік велосипедів (картки)

```bash
curl http://localhost:8080/api/bikes
```

Очікувана відповідь (скорочено):
```json
[
  { "id": 1, "model": "Trail 29 Pro", "brand": "Ridex", "type": "Гірський", "price": 41900.00, "imageUrl": "/images/bike-trail.svg" },
  { "id": 2, "model": "Road Carbon 2", "brand": "Velora", "type": "Шосейний", "price": 86500.00, "imageUrl": "/images/bike-road.svg" }
]
```

---

## 2. Перелік із фільтром за типом

```bash
curl "http://localhost:8080/api/bikes?type=Гірський"
```

Поверне лише велосипеди цього типу. Доступні типи у стартових даних:
`Гірський`, `Шосейний`, `Міський`, `Гравійний`, `Підлітковий`.
Фільтр нечутливий до регістру, порожнє значення означає «усі».

---

## 3. Один велосипед з деталями

```bash
curl http://localhost:8080/api/bikes/1
```

```json
{
  "id": 1,
  "model": "Trail 29 Pro",
  "brand": "Ridex",
  "type": "Гірський",
  "price": 41900.00,
  "frameMaterial": "Алюміній",
  "frameSize": "L",
  "wheelSize": 29.0,
  "gears": 12,
  "weightKg": 13.40,
  "year": 2024,
  "imageUrl": "/images/bike-trail.svg",
  "images": ["/images/bike-trail.svg", "/images/bike-trail-2.svg"],
  "description": "Гірський велосипед на алюмінієвій рамі ...",
  "inStock": 6
}
```

---

## 4. Створити велосипед (POST → 201 Created)

```bash
curl -i -X POST http://localhost:8080/api/bikes \
  -H "Content-Type: application/json" \
  -d '{
    "model": "Enduro 29",
    "brand": "Ridex",
    "type": "Гірський",
    "price": 52000.00,
    "frameMaterial": "Алюміній",
    "frameSize": "M",
    "wheelSize": 29.0,
    "gears": 12,
    "weightKg": 14.10,
    "year": 2025,
    "imageUrl": "/images/bike-trail.svg",
    "images": ["/images/bike-trail.svg"],
    "description": "Ендуро-версія на довгоходій вилці 160 мм.",
    "inStock": 2
  }'
```

Очікувано: `HTTP/1.1 201` і JSON створеного велосипеда з призначеним `id` (наступний у черзі — 6).
У тілі запиту `id` не передається: його призначає сервер, тому вхідний DTO `BikeRequest`
цього поля взагалі не має.

---

## 5. Оновити велосипед (PUT → 200 OK)

```bash
curl -i -X PUT http://localhost:8080/api/bikes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "model": "Trail 29 Pro 2025",
    "brand": "Ridex",
    "type": "Гірський",
    "price": 44500.00,
    "frameMaterial": "Алюміній",
    "frameSize": "L",
    "wheelSize": 29.0,
    "gears": 12,
    "weightKg": 13.20,
    "year": 2025,
    "imageUrl": "/images/bike-trail.svg",
    "images": ["/images/bike-trail.svg", "/images/bike-trail-2.svg"],
    "description": "Оновлена модель із трансмісією 1×12 і новою вилкою.",
    "inStock": 5
  }'
```

Очікувано: `HTTP/1.1 200` і велосипед з тим самим `id = 1`, але новими полями.

Неіснуючий id:

```bash
curl -i -X PUT http://localhost:8080/api/bikes/999 \
  -H "Content-Type: application/json" \
  -d '{"model":"Немає такого","brand":"Ridex","type":"Гірський","price":1000.00}'
```

Очікувано: `HTTP/1.1 404` — сервіс спершу перевіряє наявність і кидає `BikeNotFoundException`
(а не створює новий запис).

---

## 6. Видалити велосипед (DELETE → 204 No Content)

```bash
curl -i -X DELETE http://localhost:8080/api/bikes/2
```

Очікувано: `HTTP/1.1 204` і порожнє тіло.

Повторний виклик того самого запиту:

```bash
curl -i -X DELETE http://localhost:8080/api/bikes/2
```

Очікувано: `HTTP/1.1 404` — видаляти вже нічого.

---

## 7. Статичні зображення

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/images/bike-trail.svg
```

Очікувано: `200`. Файли лежать у `src/main/resources/static/images/`.

---

## Підсумкова таблиця

| Операція | Запит | Код |
|---|---|---|
| перелік | `GET /api/bikes` | 200 |
| фільтр за типом | `GET /api/bikes?type=Гірський` | 200 |
| один велосипед | `GET /api/bikes/{id}` | 200 / 404 |
| створення | `POST /api/bikes` | 201 |
| оновлення | `PUT /api/bikes/{id}` | 200 / 404 |
| видалення | `DELETE /api/bikes/{id}` | 204 / 404 |

> Дані зберігаються в пам'яті: після перезапуску застосунку сховище знову
> наповнюється п'ятьма стартовими велосипедами з `BikeSeeder`.
