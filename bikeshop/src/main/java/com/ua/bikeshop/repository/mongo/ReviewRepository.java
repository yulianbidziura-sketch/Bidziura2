package com.ua.bikeshop.repository.mongo;

import com.ua.bikeshop.domain.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Сховище відгуків у MongoDB (ГЗ 2/4).
 *
 * ШАР: repository/mongo. Успадкування від {@link MongoRepository} дає готові
 * {@code save}, {@code findById}, {@code deleteAll} — реалізацію генерує Spring
 * Data, так само як і для JPA.
 *
 * Окремого доменного інтерфейсу-адаптера тут навмисно немає, на відміну від
 * велосипедів: у Модулі 1 той інтерфейс робився, щоб потім підмінити сховище,
 * і це справді знадобилось. Для відгуків альтернативного сховища не
 * передбачено, тому зайвий прошарок був би ускладненням без користі.
 *
 * {@code findByBikeIdOrderByCreatedAtDesc} — похідний метод: запит до колекції
 * Spring Data будує з самої назви.
 *
 * Окремого {@code @Profile} тут не треба: під профілем {@code inmemory}
 * автоконфігурацію Mongo вимкнено повністю, тож бін репозиторію взагалі не
 * створюється.
 */
public interface ReviewRepository extends MongoRepository<Review, String> {

    /**
     * Відгуки про один велосипед, найновіші спершу.
     *
     * Другий критерій — спадання {@code id} — не зайвий: MongoDB зберігає дату з
     * точністю до мілісекунди, тож два відгуки, додані в одну мілісекунду, мали б
     * невизначений порядок. {@code ObjectId} містить лічильник створення, тому за
     * ним порядок завжди однозначний.
     */
    List<Review> findByBikeIdOrderByCreatedAtDescIdDesc(Long bikeId);

    /** Скільки відгуків про велосипед — для середньої оцінки й лічильників. */
    long countByBikeId(Long bikeId);
}
