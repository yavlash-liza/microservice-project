package com.yavlash.microservices.core.review.controller;

import com.yavlash.api.controller.ReviewController;
import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.exception.InvalidInputException;
import com.yavlash.microservices.core.review.services.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static java.util.logging.Level.FINE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ReviewControllerImpl implements ReviewController {
    private final ReviewService service;

    @Autowired
    @Qualifier("jdbcScheduler")
    private Scheduler jdbcScheduler;

    @Override
    public Mono<ReviewDto> createReview(ReviewDto body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        return Mono.fromCallable(() -> service.createReview(body))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Flux<ReviewDto> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get reviews for product with id={}", productId);
        return Mono.fromCallable(() -> service.findReviewsByProductId(productId))
                .flatMapMany(Flux::fromIterable)
                .log(log.getName(), FINE)
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        return Mono.fromRunnable(() -> service.deleteReviewsByProductId(productId)).subscribeOn(jdbcScheduler).then();
    }
}