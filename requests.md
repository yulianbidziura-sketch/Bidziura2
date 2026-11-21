# Приклади запитів — VeloUA (заняття 1/1)

Бек має бути запущений: `./mvnw spring-boot:run` (порт 8080, JDK 21).

На 1/1 реалізовано **два ендпоінти читання**: перелік велосипедів і один велосипед
з деталями. Створення/оновлення/видалення — на занятті 1/2.

---

## 1. Перелік велосипедів (картки)

```bash
curl http://localhost:8080/api/bikes
```

Очікувана відповідь (скорочено):
```json
[
  { "id": 1, "model": "Trail 29 Pro", "brand": "Ridex", "price": 41900.00, "imageUrl": "/images/bike-trail.svg" },
  { "id": 2, "model": "Road Carbon 2", "brand": "Velora", "price": 86500.00, "imageUrl": "/images/bike-road.svg" }
]
```

Красивий вивід (якщо встановлено `jq`):
```bash
curl -s http://localhost:8080/api/bikes | jq
```

---

## 2. Один велосипед з деталями

```bash
curl http://localhost:8080/api/bikes/1
```

Очікувана відповідь:
```json
{
  "id": 1,
  "model": "Trail 29 Pro",
  "brand": "Ridex",
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

Картка в переліку містить лише 5 полів, сторінка велосипеда — усі: це два різні
DTO (`BikeCardDto` і `BikeDetailDto`) на один доменний клас `Bike`.

---

## 3. Неіснуючий велосипед → 404

```bash
curl -i http://localhost:8080/api/bikes/999
```

Очікувано: `HTTP/1.1 404`. Сервіс кидає `BikeNotFoundException`, позначений
`@ResponseStatus(HttpStatus.NOT_FOUND)`, тому Spring сам віддає цей статус.

---

## 4. Статичні зображення

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/images/bike-trail.svg
```

Очікувано: `200`. Файли лежать у `src/main/resources/static/images/`.
