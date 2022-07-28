package com.yavlash.microservices.core.product.config;

import com.yavlash.api.controller.ProductController;
import com.yavlash.api.dto.ProductDto;
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
    private final ProductController productController;

    @Bean
    public Consumer<Event<Integer, ProductDto>> messageProcessor() {
        return event -> {
            log.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE -> {
                    ProductDto productDto = event.getData();
                    log.info("Create product with ID: {}", productDto.getProductId());
                    productController.createProduct(productDto).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    log.info("Delete recommendations with ProductID: {}", productId);
                    productController.deleteProduct(productId).block();
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