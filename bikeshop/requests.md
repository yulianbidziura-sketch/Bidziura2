# Приклади запитів — VeloUA (CRUD, заняття 1/2 + ПЗ 1/3)

Бек має бути запущений: `./mvnw spring-boot:run` (порт 8080, JDK 21).

Доступні всі операції CRUD: перелік, фільтри, один велосипед, створення,
оновлення, видалення. **ПЗ 1/3** додало поле `color` і другий фільтр
`?color=`, який працює і сам по собі, і разом із `?type=`.

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
curl -G --data-urlencode "type=Гірський" http://localhost:8080/api/bikes
```

Те саме з уже закодованим значенням (якщо зручніше одним рядком):

```bash
curl "http://localhost:8080/api/bikes?type=%D0%93%D1%96%D1%80%D1%81%D1%8C%D0%BA%D0%B8%D0%B9"
```

Поверне лише велосипеди цього типу. Доступні типи у стартових даних:
`Гірський`, `Шосейний`, `Міський`, `Гравійний`, `Підлітковий`.
Фільтр нечутливий до регістру, порожнє значення означає «усі».

> ⚠️ Кирилицю в рядку запиту треба **кодувати у percent-encoding**: за RFC 7230/3986
> у request target дозволені лише ASCII-символи, і якщо вставити кирилицю як є
> (`curl "...?type=Гірський"`), Tomcat відхилить запит зі статусом **400** ще до
> контролера: *Invalid character found in the request target*. Ключ `-G
> --data-urlencode` змушує curl закодувати значення самостійно; браузер,
> HTTP-клієнт IntelliJ і fetch у JavaScript роблять це автоматично.

---

## 2a. Перелік із фільтром за кольором (ПЗ 1/3)

```bash
curl -G --data-urlencode "color=Чорний" http://localhost:8080/api/bikes
```

Кольори у стартових даних: `Чорний`, `Червоний`, `Бірюзовий`, `Пісочний`, `Синій`.

## 2b. Обидва фільтри разом

```bash
curl -G --data-urlencode "type=Гірський" --data-urlencode "color=Чорний" \
  http://localhost:8080/api/bikes
```

Критерії складаються: повернуться велосипеди, у яких збігається і тип, і колір.
Якщо такого поєднання немає — порожній масив `[]` зі статусом 200 (це не помилка:
ресурс-колекція існує, просто порожня).

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
  "inStock": 6,
  "color": "Чорний"
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
    "inStock": 2,
    "color": "Чорний"
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
    "inStock": 5,
    "color": "Чорний"
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

## 6a. Помилки мають єдиний формат (ГЗ 1/4)

Усі помилки API повертаються одним типом `ErrorResponse`
(`status`, `error`, `message`, `details`) — його формує глобальний обробник
`GlobalExceptionHandler` (`@RestControllerAdvice`).

**Неіснуючий id → 404:**

```bash
curl -i http://localhost:8080/api/bikes/999
```

```json
{"status":404,"error":"Not Found","message":"Велосипед з id=999 не знайдено","details":[]}
```

**Невалідне тіло запиту → 400** (порожня модель, від'ємні ціна й залишок):

```bash
curl -i -X POST http://localhost:8080/api/bikes \
  -H "Content-Type: application/json" \
  -d '{"model":"","brand":"Ridex","type":"Гірський","price":-100,"inStock":-1}'
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Помилка валідації вхідних даних",
  "details": [
    "inStock: залишок не може бути від'ємним",
    "model: модель обов'язкова",
    "price: ціна має бути додатною"
  ]
}
```

Правила перевірки стоять на вхідному DTO `BikeRequest` (`@NotBlank` на моделі,
бренді й типі, `@Positive` на ціні, `@PositiveOrZero` на залишку), а в контролері
тіло позначено `@Valid`. Невалідний запит **до сервісу не доходить**.

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
| фільтр за типом | `GET /api/bikes?type=<закодоване значення>` | 200 |
| фільтр за кольором (ПЗ 1/3) | `GET /api/bikes?color=<закодоване значення>` | 200 |
| обидва фільтри разом | `GET /api/bikes?type=...&color=...` | 200 |
| один велосипед | `GET /api/bikes/{id}` | 200 / 404 |
| створення | `POST /api/bikes` | 201 |
| оновлення | `PUT /api/bikes/{id}` | 200 / 404 |
| видалення | `DELETE /api/bikes/{id}` | 204 / 404 |
| невалідне тіло (ГЗ 1/4) | `POST` або `PUT` з порушенням правил | 400 |

> Дані зберігаються в пам'яті: після перезапуску застосунку сховище знову
> наповнюється п'ятьма стартовими велосипедами з `BikeSeeder`.
