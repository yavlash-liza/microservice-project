package com.yavlash.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.yavlash.api.core.recommendation.Recommendation;
import com.yavlash.microservices.core.recommendation.persistence.RecommendationEntity;
import com.yavlash.microservices.core.recommendation.services.RecommendationMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class MapperTests {
    private final RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {
        //given && when
        Recommendation api = new Recommendation(1, 2, "a", 4, "C", "adr");
        RecommendationEntity entity = mapper.apiToEntity(api);
        Recommendation api2 = mapper.entityToApi(entity);

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
        Recommendation api = new Recommendation(1, 2, "a", 4, "C", "adr");
        List<Recommendation> apiList = Collections.singletonList(api);
        List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);
        RecommendationEntity entity = entityList.get(0);
        List<Recommendation> api2List = mapper.entityListToApiList(entityList);
        Recommendation api2 = api2List.get(0);

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