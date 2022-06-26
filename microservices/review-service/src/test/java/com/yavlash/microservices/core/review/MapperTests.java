package com.yavlash.microservices.core.review;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.yavlash.api.core.review.Review;
import com.yavlash.microservices.core.review.persistence.ReviewEntity;
import com.yavlash.microservices.core.review.services.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class MapperTests {
    private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {
        //given && when
        Review api = new Review(1, 2, "a", "s", "C", "adr");
        ReviewEntity entity = mapper.apiToEntity(api);
        Review api2 = mapper.entityToApi(entity);

        //then
        assertNotNull(mapper);
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getSubject(), entity.getSubject());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getSubject(), api2.getSubject());
        assertEquals(api.getContent(), api2.getContent());
        assertNull(api2.getServiceAddress());
    }

    @Test
    void mapperListTests() {
        //given && when
        Review api = new Review(1, 2, "a", "s", "C", "adr");
        List<Review> apiList = Collections.singletonList(api);
        List<ReviewEntity> entityList = mapper.apiListToEntityList(apiList);
        ReviewEntity entity = entityList.get(0);
        List<Review> api2List = mapper.entityListToApiList(entityList);
        Review api2 = api2List.get(0);

        //then
        assertNotNull(mapper);
        assertEquals(apiList.size(), entityList.size());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getSubject(), entity.getSubject());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(apiList.size(), api2List.size());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getSubject(), api2.getSubject());
        assertEquals(api.getContent(), api2.getContent());
        assertNull(api2.getServiceAddress());
    }
}