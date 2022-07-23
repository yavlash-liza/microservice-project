package com.yavlash.microservices.core.recommendation.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.microservices.core.recommendation.entity.Recommendation;
import com.yavlash.microservices.core.recommendation.services.RecommendationMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class RecommendationMapperTest {
    private final RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {
        //given && when
        RecommendationDto api = new RecommendationDto()
                .setProductId(1)
                .setRecommendationId(2)
                .setAuthor("a")
                .setContent("C")
                .setRate(4)
                .setServiceAddress("adr");
        Recommendation entity = mapper.fromDto(api);
        RecommendationDto api2 = mapper.toDto(entity);

        //then
        assertNotNull(mapper);
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRate(), api2.getRate());
        assertEquals(api.getContent(), api2.getContent());
        assertNull(api2.getServiceAddress());
    }

    @Test
    void mapperListTests() {
        //given && when
        RecommendationDto api = new RecommendationDto()
                .setProductId(1)
                .setRecommendationId(2)
                .setAuthor("a")
                .setContent("C")
                .setRate(4)
                .setServiceAddress("adr");
        List<RecommendationDto> apiList = Collections.singletonList(api);
        List<Recommendation> entityList = mapper.fromListDto(apiList);
        Recommendation entity = entityList.get(0);
        List<RecommendationDto> api2List = mapper.toListDto(entityList);
        RecommendationDto api2 = api2List.get(0);

        //then
        assertNotNull(mapper);
        assertEquals(apiList.size(), entityList.size());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRating());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(apiList.size(), api2List.size());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRate(), api2.getRate());
        assertEquals(api.getContent(), api2.getContent());
        assertNull(api2.getServiceAddress());
    }
}