package com.yavlash.microservices.core.review.services;

import com.yavlash.api.dto.ReviewDto;
import com.yavlash.microservices.core.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "serviceAddress", ignore = true)
    ReviewDto toDto(Review entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Review fromDto(ReviewDto api);

    List<ReviewDto> toListDto(List<Review> entity);

    List<Review> fromListDto(List<ReviewDto> api);
}