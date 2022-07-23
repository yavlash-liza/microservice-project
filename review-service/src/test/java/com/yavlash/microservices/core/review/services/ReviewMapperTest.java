package com.yavlash.microservices.core.review.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.yavlash.api.dto.ReviewDto;
import com.yavlash.microservices.core.review.entity.Review;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class ReviewMapperTest {
    private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {
        //given && when
        ReviewDto api = new ReviewDto()
                .setProductId(1)
                .setReviewId(2)
                .setAuthor("a")
                .setSubject("s")
                .setContent("C")
                .setServiceAddress("adr");
        Review entity = mapper.fromDto(api);
        ReviewDto api2 = mapper.toDto(entity);

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
        ReviewDto api = new ReviewDto()
                .setProductId(1)
                .setReviewId(2)
                .setAuthor("a")
                .setSubject("s")
                .setContent("C")
                .setServiceAddress("adr");
        List<ReviewDto> apiList = Collections.singletonList(api);
        List<Review> entityList = mapper.fromListDto(apiList);
        Review entity = entityList.get(0);
        List<ReviewDto> api2List = mapper.toListDto(entityList);
        ReviewDto api2 = api2List.get(0);

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