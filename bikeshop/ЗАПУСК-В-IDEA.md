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

## 2a. Підняти базу даних (ПЗ 2/3)

З ПЗ 2/3 велосипеди зберігаються в PostgreSQL, а з ГЗ 2/4 відгуки — ще й у MongoDB,
тому перед запуском треба підняти обидві бази. У корені проєкту є `docker-compose.yml`:
відкрити його в IDEA і натиснути зелений ▶ біля `services` (потрібен запущений
Docker Desktop) — або в терміналі:

```bash
docker compose up -d
```

Перевірити, що контейнери `veloua-postgres` і `veloua-mongo` у стані **healthy**:
вкладка **Services → Docker → Containers**.

Міграції Liquibase виконаються самі під час старту застосунку — у консолі
з'являться рядки `ChangeSet ... ran successfully` і підсумок `UPDATE SUMMARY`.

> Якщо Docker недоступний, застосунок можна запустити без бази взагалі —
> конфігурацією **VeloUA — у пам'яті (без БД)** (профіль `inmemory`).

## 3. Запустити застосунок

У списку конфігурацій угорі праворуч уже є три готові:

- **VeloUA — запуск** — піднімає Spring Boot (клас `BikeshopApplication`) з PostgreSQL;
- **VeloUA — у пам'яті (без БД)** — той самий застосунок під профілем `inmemory`;
- **VeloUA — усі тести** — проганяє всі 106 тестів (для них теж потрібен Docker:
  інтеграційні піднімають PostgreSQL і MongoDB через Testcontainers).

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

1. `GET /api/bikes` — перелік каталогу (26 велосипедів, картки по 6 полів);
2. `GET /api/bikes?type=Гірський` — фільтр за типом (запит № 2 в `api.http`;
   якщо клієнт не закодує кирилицю — поруч лежить запит № 2a з готовим
   percent-encoding, бо Tomcat не пускає не-ASCII у рядку запиту);
3. `GET /api/bikes/1` — повні дані одного велосипеда (15 полів разом із галереєю);
4. `GET /api/bikes/999` — **404**;
5. `POST /api/bikes` — **201**, у відповіді новий `id = 27` (а не 1 — seed-міграції
   зсунули лічильник ідентичності);
6. `PUT /api/bikes/1` — **200**, поля змінилися, `id` той самий;
7. `PUT /api/bikes/999` — **404**;
8. `DELETE /api/bikes/2` — **204**;
9. `DELETE /api/bikes/2` ще раз — **404**;
10. `GET /api/bikes/page?page=0&size=5` і `?page=1&size=5` — дві різні сторінки
    з однаковими `totalElements: 26` і `totalPages: 6` (ПЗ 2/3);
11. `GET /api/bikes/page?size=3&sort=price,desc` — сортування;
12. `GET /api/bikes/page?type=Гірський&size=2` — фільтр разом зі сторінкою
    (`totalElements: 6` — рахується по фільтру);
13. `GET /api/bikes/discounted` — читання SQL-view;
14. `GET /api/bikes/count?type=Гірський` — підрахунок збереженою функцією;
15. `GET /api/bikes/search?q=Гравійний` — повнотекстовий пошук; показати, що він
    знаходить і за словом з опису (`?q=карбоновою`), а за підрядком (`?q=рам`) —
    ні, бо це пошук по словах, а не `LIKE` (ГЗ 2/4);
16. `POST /api/bikes/1/reviews` → **201**, далі `GET /api/bikes/1/reviews` — відгуки
    з MongoDB, найновіші спершу; `POST /api/bikes/999/reviews` → **404**
    (зовнішнього ключа між базами немає, існування велосипеда перевіряє застосунок);
    оцінка `9` → **400**.

Ті самі запити у вигляді `curl` — у `requests.md`.

Показати, що схему створив Liquibase (вкладка **Services → Docker** або термінал):

```bash
docker exec -it veloua-postgres psql -U veloua -d veloua \
  -c "SELECT id, author, filename FROM databasechangelog ORDER BY orderexecuted;"
```

Показати, що дані тепер постійні: створити велосипед через `POST`, перезапустити
застосунок (■ потім ▶) і відкрити його за `id` — запис на місці.

Зображення віддаються як статика: <http://localhost:8080/images/bike-trail.svg>.

## 5. Показати тести

Запустити конфігурацію **VeloUA — усі тести** (або правою кнопкою на теці
`src/test/java` → `Run 'All Tests'`). Очікувано: **106 passed**.

Перший прогін довший: Testcontainers піднімає PostgreSQL і MongoDB у контейнерах
(потрібен запущений Docker; образ `mongo:7` завантажується один раз). Тести
`BikeApiInMemoryProfileTest`, `BikeControllerTest` і `ReviewControllerTest` працюють
і без Docker.

## 6. Показати, що зроблено на кожному занятті

```bash
git log --oneline --decorate     # уся історія з тегами
git checkout gz-1.1              # стан після заняття 1/1 (лише два GET)
git checkout main                # повернутись до повного CRUD
```

У IDEA те саме — вкладка `Git` унизу: гілка `main`, теги `gz-1.1`, `gz-1.2`,
`pz-1.3`, `gz-1.4`, `pz-1.5`, `pz-2.3`, `gz-2.4`.
