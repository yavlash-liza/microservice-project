package com.yavlash.api.controller;

import com.yavlash.api.dto.RecommendationDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationController {
    Mono<RecommendationDto> createRecommendation(RecommendationDto body);

    @GetMapping(
            value = "/recommendation",
            produces = "application/json"
    )
    Flux<RecommendationDto> getRecommendations(
            @RequestParam(value = "productId", required = true) int productId);

    Mono<Void> deleteRecommendations(int productId);
}