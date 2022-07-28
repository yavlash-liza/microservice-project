package com.yavlash.microservices.core.recommendation.controller;

import com.yavlash.api.controller.RecommendationController;
import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.api.exception.InvalidInputException;
import com.yavlash.microservices.core.recommendation.entity.Recommendation;
import com.yavlash.microservices.core.recommendation.repository.RecommendationRepository;
import com.yavlash.microservices.core.recommendation.services.RecommendationMapper;
import com.yavlash.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RecommendationControllerImpl implements RecommendationController {
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Override
    public Mono<RecommendationDto> createRecommendation(RecommendationDto body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        Recommendation entity = mapper.fromDto(body);
        return repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(mapper::toDto);
    }

    @Override
    public Flux<RecommendationDto> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get recommendations for product with id={}", productId);
        return repository.findByProductId(productId)
                .log(log.getName(), FINE)
                .map(mapper::toDto)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private RecommendationDto setServiceAddress(RecommendationDto e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}