package com.yavlash.microservices.core.review.services;

import java.util.function.Consumer;

import com.yavlash.api.core.review.Review;
import com.yavlash.api.core.review.ReviewController;
import com.yavlash.api.event.Event;
import com.yavlash.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final ReviewController reviewService;

    @Autowired
    public MessageProcessorConfig(ReviewController reviewService) {
        this.reviewService = reviewService;
    }

    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE -> {
                    Review review = event.getData();
                    LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
                    reviewService.createReview(review).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    LOG.info("Delete reviews with ProductID: {}", productId);
                    reviewService.deleteReviews(productId).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            LOG.info("Message processing done!");
        };
    }
}