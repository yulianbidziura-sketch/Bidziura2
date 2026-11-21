# Як запустити й показати проєкт у IntelliJ IDEA

## 1. Відкрити проєкт

`File → Open…` → вибрати **папку** `bikeshop` (не окремий файл) → `Open`.

IDEA сама побачить `pom.xml` і підтягне залежності (внизу праворуч буде смужка
«Resolving Maven dependencies»). Дочекатися, доки вона зникне.

Якщо вгорі з'явиться жовта смужка «Maven build script found» — натиснути **Load**.

## 2. Перевірити JDK

`File → Project Structure… → Project`:

- **SDK:** будь-який Java 21 або новіший (на цьому комп'ютері стоїть 26);
- **Language level:** 21 — підтягується з `pom.xml` (`<java.version>21</java.version>`).

Якщо SDK не вибрано: `Add SDK → Download JDK… → версія 21, Eclipse Temurin`.

## 3. Запустити застосунок

У списку конфігурацій угорі праворуч уже є дві готові:

- **VeloUA — запуск** — піднімає Spring Boot (клас `BikeshopApplication`);
- **VeloUA — усі тести** — проганяє всі 24 тести.

Вибрати **VeloUA — запуск** і натиснути ▶ (або `Ctrl+R`).

У консолі має з'явитися:

```
Tomcat started on port 8080 (http)
Started BikeshopApplication in ... seconds
```

Якщо конфігурацій у списку немає — відкрити `src/main/java/com/ua/bikeshop/BikeshopApplication.java`
і натиснути зелений трикутник біля методу `main`.

> Порт 8080 зайнятий? Звільнити: `lsof -ti:8080 | xargs kill -9`,
> або змінити порт у `src/main/resources/application.yaml`.

## 4. Показати ендпоінти

Найзручніше — прямо в IDEA, файл **`api.http`**: відкрити його й тиснути ▶ біля
кожного запиту. Відповідь і код статусу з'являються в окремій панелі.

Порядок для демонстрації:

1. `GET /api/bikes` — перелік із п'яти велосипедів (картки: 6 полів);
2. `GET /api/bikes?type=Гірський` — фільтр за типом (запит № 2 в `api.http`;
   якщо клієнт не закодує кирилицю — поруч лежить запит № 2a з готовим
   percent-encoding, бо Tomcat не пускає не-ASCII у рядку запиту);
3. `GET /api/bikes/1` — повні дані одного велосипеда (15 полів разом із галереєю);
4. `GET /api/bikes/999` — **404**;
5. `POST /api/bikes` — **201**, у відповіді новий `id = 6`;
6. `PUT /api/bikes/1` — **200**, поля змінилися, `id` той самий;
7. `PUT /api/bikes/999` — **404**;
8. `DELETE /api/bikes/2` — **204**;
9. `DELETE /api/bikes/2` ще раз — **404**.

Ті самі запити у вигляді `curl` — у `requests.md`.

Зображення віддаються як статика: <http://localhost:8080/images/bike-trail.svg>.

## 5. Показати тести

Запустити конфігурацію **VeloUA — усі тести** (або правою кнопкою на теці
`src/test/java` → `Run 'All Tests'`). Очікувано: **24 passed**.

## 6. Показати, що зроблено на кожному занятті

```bash
git log --oneline --decorate     # уся історія з тегами
git checkout gz-1.1              # стан після заняття 1/1 (лише два GET)
git checkout main                # повернутись до повного CRUD
```

У IDEA те саме — вкладка `Git` унизу: гілка `main`, теги `gz-1.1` і `gz-1.2`.
