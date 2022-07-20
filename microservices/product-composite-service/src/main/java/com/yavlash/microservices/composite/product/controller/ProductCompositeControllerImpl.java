package com.yavlash.microservices.composite.product.controller;

import com.yavlash.api.controller.ProductCompositeController;
import com.yavlash.api.dto.ProductDto;
import com.yavlash.api.dto.ProductListDto;
import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.api.dto.RecommendationListDto;
import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.dto.ReviewListDto;
import com.yavlash.api.util.ServiceAddresses;
import com.yavlash.microservices.composite.product.services.ProductCompositeIntegration;
import com.yavlash.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINE;

@Slf4j
@RestController
public class ProductCompositeControllerImpl implements ProductCompositeController {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeControllerImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public Mono<Void> createProduct(ProductListDto body) {
        try {
            List<Mono> monoList = new ArrayList<>();
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());
            ProductDto productDto = new ProductDto()
                    .setProductId(body.getProductId())
                    .setName(body.getName())
                    .setWeight(body.getWeight())
                    .setServiceAddress(null);
            monoList.add(integration.createProduct(productDto));
            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    RecommendationDto recommendationDto = new RecommendationDto()
                            .setProductId(body.getProductId())
                            .setRecommendationId(r.getRecommendationId())
                            .setAuthor(r.getAuthor())
                            .setRate(r.getRate())
                            .setContent(r.getContent())
                            .setServiceAddress(null);
                    monoList.add(integration.createRecommendation(recommendationDto));
                });
            }
            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    ReviewDto reviewDto = new ReviewDto()
                            .setProductId(body.getProductId())
                            .setReviewId(r.getReviewId())
                            .setAuthor(r.getAuthor())
                            .setSubject(r.getSubject())
                            .setContent(r.getContent())
                            .setServiceAddress(null);
                    monoList.add(integration.createReview(reviewDto));
                });
            }
            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());
            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();
        } catch (RuntimeException re) {
            log.warn("createCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    public Mono<ProductListDto> getProduct(int productId) {
        log.info("Will get composite product info for product.id={}", productId);
        return Mono.zip(
                        values -> createProductAggregate((ProductDto) values[0], (List<RecommendationDto>) values[1], (List<ReviewDto>) values[2], serviceUtil.getServiceAddress()),
                        integration.getProduct(productId),
                        integration.getRecommendations(productId).collectList(),
                        integration.getReviews(productId).collectList())
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log(log.getName(), FINE);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        try {
            log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);
            return Mono.zip(
                            r -> "",
                            integration.deleteProduct(productId),
                            integration.deleteRecommendations(productId),
                            integration.deleteReviews(productId))
                    .doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                    .log(log.getName(), FINE).then();
        } catch (RuntimeException re) {
            log.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    private ProductListDto createProductAggregate(ProductDto productDto, List<RecommendationDto> recommendationDtos, List<ReviewDto> reviewDtos, String serviceAddress) {
        int productId = productDto.getProductId();
        String name = productDto.getName();
        int weight = productDto.getWeight();
        List<RecommendationListDto> recommendationSummaries = (recommendationDtos == null) ? null :
                recommendationDtos.stream()
                        .map(r -> new RecommendationListDto()
                                .setRecommendationId(r.getRecommendationId())
                                .setAuthor(r.getAuthor())
                                .setRate(r.getRate())
                                .setContent(r.getContent()))
                        .collect(Collectors.toList());
        List<ReviewListDto> reviewSummaries = (reviewDtos == null) ? null :
                reviewDtos.stream()
                        .map(r -> new ReviewListDto()
                                .setReviewId(r.getReviewId())
                                .setAuthor(r.getAuthor())
                                .setSubject(r.getSubject())
                                .setContent(r.getContent()))
                        .collect(Collectors.toList());
        String productAddress = productDto.getServiceAddress();
        String reviewAddress = (reviewDtos != null && reviewDtos.size() > 0) ? reviewDtos.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendationDtos != null && recommendationDtos.size() > 0) ? recommendationDtos.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses()
                .setCmp(serviceAddress)
                .setPro(productAddress)
                .setRev(reviewAddress)
                .setRec(recommendationAddress);
        return new ProductListDto()
                .setProductId(productId)
                .setName(name)
                .setWeight(weight)
                .setRecommendations(recommendationSummaries)
                .setReviews(reviewSummaries)
                .setServiceAddresses(serviceAddresses);
    }
}