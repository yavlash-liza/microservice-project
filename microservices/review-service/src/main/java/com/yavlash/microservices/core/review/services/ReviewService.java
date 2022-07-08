package com.yavlash.microservices.core.review.services;

import com.yavlash.api.core.review.Review;
import com.yavlash.microservices.core.review.controller.ReviewControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface ReviewService {
    Logger LOG = LoggerFactory.getLogger(ReviewControllerImpl.class);
    Review createReview(Review review);
    List<Review> findReviewsByProductId(int productId);
    void deleteReviewsByProductId(int productId);
}