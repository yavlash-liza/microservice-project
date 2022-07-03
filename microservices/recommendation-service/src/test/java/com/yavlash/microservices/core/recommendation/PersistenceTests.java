package com.yavlash.microservices.core.recommendation;

import com.yavlash.microservices.core.recommendation.persistence.RecommendationEntity;
import com.yavlash.microservices.core.recommendation.persistence.RecommendationRepository;
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
class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        savedEntity = repository.save(entity).block();
        assertEqualsRecommendation(entity, savedEntity);
    }

    @Test
    void create() {
        //given && when
        RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        repository.save(newEntity).block();
        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();

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
        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();

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
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

        //then
        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test
    void optimisticLockError() {
        // given && when
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();
        if (entity1 != null) {
            entity1.setAuthor("a1");
            repository.save(entity1).block();
        }
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();

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

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}