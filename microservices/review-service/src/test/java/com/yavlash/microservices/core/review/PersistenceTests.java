package com.yavlash.microservices.core.review;

import com.yavlash.microservices.core.review.entity.Review;
import com.yavlash.microservices.core.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceTests extends MySqlTestBase {

    @Autowired
    private ReviewRepository repository;

    private Review savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
        Review entity = new Review()
                .setReviewId(1)
                .setProductId(2)
                .setContent("c")
                .setAuthor("a")
                .setSubject("s");
        savedEntity = repository.save(entity);
        assertEqualsReview(entity, savedEntity);
    }

    @Test
    void create() {
        //given && when
        Review newEntity = new Review()
                .setReviewId(1)
                .setProductId(3)
                .setContent("c")
                .setAuthor("a")
                .setSubject("s");
        repository.save(newEntity);
        Review foundEntity = repository.findById(newEntity.getId()).get();

        //then
        assertEqualsReview(newEntity, foundEntity);
        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        //given && when
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);
        Review foundEntity = repository.findById(savedEntity.getId()).get();

        //then
        assertEquals(1, (long) foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        //given && when
        repository.delete(savedEntity);

        //then
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        //given && when
        List<Review> entityList = repository.findByProductId(savedEntity.getProductId());

        //then
        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            Review entity = new Review()
                    .setReviewId(1)
                    .setProductId(2)
                    .setContent("c")
                    .setAuthor("a")
                    .setSubject("s");
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        // given && then
        Review entity1 = repository.findById(savedEntity.getId()).get();
        Review entity2 = repository.findById(savedEntity.getId()).get();
        entity1.setAuthor("a1");
        repository.save(entity1);

        // then
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            repository.save(entity2);
        });
        Review updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int) updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsReview(Review expectedEntity, Review actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}