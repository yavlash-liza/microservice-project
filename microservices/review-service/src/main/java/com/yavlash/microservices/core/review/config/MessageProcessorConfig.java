package com.yavlash.microservices.core.review.config;

import com.yavlash.api.controller.ReviewController;
import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.event.Event;
import com.yavlash.api.exceptions.EventProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class MessageProcessorConfig {
    private final ReviewController reviewService;

    @Autowired
    public MessageProcessorConfig(ReviewController reviewService) {
        this.reviewService = reviewService;
    }

    @Bean
    public Consumer<Event<Integer, ReviewDto>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE -> {
                    ReviewDto reviewDto = event.getData();
                    log.info("Create review with ID: {}/{}", reviewDto.getProductId(), reviewDto.getReviewId());
                    reviewService.createReview(reviewDto).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    log.info("Delete reviews with ProductID: {}", productId);
                    reviewService.deleteReviews(productId).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            log.info("Message processing done!");
        };
    }
}