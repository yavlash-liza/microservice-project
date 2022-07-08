package com.yavlash.microservices.core.review.controller;

import com.yavlash.api.core.review.Review;
import com.yavlash.api.core.review.ReviewController;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.microservices.core.review.services.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewControllerImpl implements ReviewController {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewControllerImpl.class);
    private final ReviewService service;
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewControllerImpl(@Qualifier("jdbcScheduler") Scheduler jdbcScheduler, ReviewService service) {
        this.jdbcScheduler = jdbcScheduler;
        this.service = service;
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        return Mono.fromCallable(() -> service.createReview(body))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.info("Will get reviews for product with id={}", productId);
        return Mono.fromCallable(() -> service.findReviewsByProductId(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), FINE)
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