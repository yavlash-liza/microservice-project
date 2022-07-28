package com.yavlash.microservices.core.review.services;

import com.yavlash.api.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    ReviewDto createReview(ReviewDto reviewDto);
    List<ReviewDto> findReviewsByProductId(int productId);
    void deleteReviewsByProductId(int productId);
}