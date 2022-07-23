package com.yavlash.microservices.core.recommendation.services;

import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.microservices.core.recommendation.entity.Recommendation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    @Mapping(target = "rate", source = "entity.rating")
    @Mapping(target = "serviceAddress", ignore = true)
    RecommendationDto toDto(Recommendation entity);

    @Mapping(target = "rating", source = "api.rate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Recommendation fromDto(RecommendationDto api);

    List<RecommendationDto> toListDto(List<Recommendation> entity);

    List<Recommendation> fromListDto(List<RecommendationDto> api);
}