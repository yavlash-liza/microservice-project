package com.yavlash.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yavlash.api.controller.ProductController;
import com.yavlash.api.controller.RecommendationController;
import com.yavlash.api.controller.ReviewController;
import com.yavlash.api.dto.ProductDto;
import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.event.Event;
import com.yavlash.api.exception.InvalidInputException;
import com.yavlash.api.exception.NotFoundException;
import com.yavlash.util.http.HttpErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;

import static com.yavlash.api.event.Event.Type.CREATE;
import static com.yavlash.api.event.Event.Type.DELETE;
import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductController, RecommendationController, ReviewController {
    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";
    private final Scheduler publishEventScheduler;
    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final StreamBridge streamBridge;

    @Autowired
    public ProductCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            StreamBridge streamBridge
    ) {
        this.webClient = webClientBuilder.build();
        this.publishEventScheduler = publishEventScheduler;
        this.mapper = mapper;
        this.streamBridge = streamBridge;
    }

    @Override
    public Mono<ProductDto> createProduct(ProductDto body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<ProductDto> getProduct(int productId) {
        String url = PRODUCT_SERVICE_URL + "/product/" + productId;
        log.debug("Will call the getProduct API on URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ProductDto.class)
                .log(log.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    private Throwable handleException(Throwable e) {
        if (!(e instanceof WebClientResponseException)) {
            log.warn("Got a unexpected error: {}, will rethrow it", e.toString());
            return e;
        }
        WebClientResponseException wcre = (WebClientResponseException)e;
        switch (wcre.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));
            default:
                log.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return e;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<RecommendationDto> createRecommendation(RecommendationDto body) {
        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<RecommendationDto> getRecommendations(int productId) {
        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;
        log.debug("Will call the getRecommendations API on URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(RecommendationDto.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<ReviewDto> createReview(ReviewDto body) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event(CREATE, body.getProductId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<ReviewDto> getReviews(int productId) {
        String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;
        log.debug("Will call the getReviews API on URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(ReviewDto.class)
                .log(log.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    private void sendMessage(String bindingName, Event event) {
        log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }
}