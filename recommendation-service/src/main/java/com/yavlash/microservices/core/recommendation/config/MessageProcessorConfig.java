package com.yavlash.microservices.core.recommendation.config;

import com.yavlash.api.controller.RecommendationController;
import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.api.event.Event;
import com.yavlash.api.exception.EventProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MessageProcessorConfig {
    private final RecommendationController recommendationController;

    @Bean
    public Consumer<Event<Integer, RecommendationDto>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE -> {
                    RecommendationDto recommendationDto = event.getData();
                    log.info("Create recommendation with ID: {}/{}", recommendationDto.getProductId(), recommendationDto.getRecommendationId());
                    recommendationController.createRecommendation(recommendationDto).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    log.info("Delete recommendations with ProductID: {}", productId);
                    recommendationController.deleteRecommendations(productId).block();
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