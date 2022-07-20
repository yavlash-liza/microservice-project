package com.yavlash.microservices.core.recommendation.repository;

import com.yavlash.microservices.core.recommendation.entity.Recommendation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveCrudRepository<Recommendation, String> {
    Flux<Recommendation> findByProductId(int productId);
}