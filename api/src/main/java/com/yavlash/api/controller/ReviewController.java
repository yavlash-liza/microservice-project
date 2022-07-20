package com.yavlash.api.controller;

import com.yavlash.api.dto.ReviewDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewController {

    @PostMapping(
            value = "/review",
            consumes = "application/json")
    Mono<ReviewDto> createReview(@RequestBody ReviewDto body);

    @GetMapping(
            value = "/review",
            produces = "application/json")
    Flux<ReviewDto> getReviews(@RequestParam(value = "productId", required = true) int productId);

    Mono<Void> deleteReviews(int productId);
}