package com.yavlash.microservices.core.recommendation.repository;

import com.yavlash.microservices.core.recommendation.entity.Recommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class RecommendationRepositoryTest extends MongoDbTestBase {

    @Autowired
    private RecommendationRepository repository;

    private Recommendation savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
        Recommendation entity = new Recommendation()
                .setProductId(1)
                .setRecommendationId(2)
                .setAuthor("a")
                .setContent("c")
                .setRating(3);
        savedEntity = repository.save(entity).block();
        assertEqualsRecommendation(entity, savedEntity);
    }

    @Test
    void create() {
        //given && when
        Recommendation newEntity = new Recommendation()
                .setProductId(1)
                .setRecommendationId(3)
                .setAuthor("a")
                .setContent("c")
                .setRating(3);
        repository.save(newEntity).block();
        Recommendation foundEntity = repository.findById(newEntity.getId()).block();

        //then
        if (foundEntity != null) {
            assertEqualsRecommendation(newEntity, foundEntity);
        }
        assertEquals(2, (long) repository.count().block());
    }

    @Test
    void update() {
        //given && when
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();
        Recommendation foundEntity = repository.findById(savedEntity.getId()).block();

        //then
        if (foundEntity != null) {
            assertEquals(1, (long) foundEntity.getVersion());
        }
        if (foundEntity != null) {
            assertEquals("a2", foundEntity.getAuthor());
        }
    }

    @Test
    void delete() {
        //given && when
        repository.delete(savedEntity).block();

        //then
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        // given && when
        List<Recommendation> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

        //then
        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test
    void optimisticLockError() {
        // given && when
        Recommendation entity1 = repository.findById(savedEntity.getId()).block();
        Recommendation entity2 = repository.findById(savedEntity.getId()).block();
        if (entity1 != null) {
            entity1.setAuthor("a1");
            repository.save(entity1).block();
        }
        Recommendation updatedEntity = repository.findById(savedEntity.getId()).block();

        //then
        assertThrows(OptimisticLockingFailureException.class, () -> {
            if (entity2 != null) {
                entity2.setAuthor("a2");
                repository.save(entity2).block();
            }
        });
        assertEquals(1, (int) updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(Recommendation expectedEntity, Recommendation actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}