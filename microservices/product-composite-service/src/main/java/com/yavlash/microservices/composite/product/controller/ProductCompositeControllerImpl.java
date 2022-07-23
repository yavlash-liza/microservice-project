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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductCompositeControllerImpl implements ProductCompositeController {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Override
    public Mono<Void> createProduct(ProductListDto body) {
        try {
            List<Mono> monoList = new ArrayList<>();
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());
            ProductDto productDto = createProduct(body.getProductId(), body.getName(), body.getWeight());
            monoList.add(integration.createProduct(productDto));
            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    RecommendationDto recommendationDto = createRecommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent());
                    monoList.add(integration.createRecommendation(recommendationDto));
                });
            }
            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    ReviewDto reviewDto = createReview(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent());
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

    private ProductDto createProduct(int productId, String name, int weight) {
        return new ProductDto()
                .setProductId(productId)
                .setName(name)
                .setWeight(weight)
                .setServiceAddress(null);
    }

    private RecommendationDto createRecommendation(int productId, int recommendationId, String author, int rate, String content) {
        return new RecommendationDto()
                .setProductId(productId)
                .setRecommendationId(recommendationId)
                .setAuthor(author)
                .setRate(rate)
                .setContent(content)
                .setServiceAddress(null);
    }

    private ReviewDto createReview(int productId, int reviewId, String author, String subject, String content) {
        return new ReviewDto()
                .setProductId(productId)
                .setReviewId(reviewId)
                .setAuthor(author)
                .setSubject(subject)
                .setContent(content)
                .setServiceAddress(null);
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
        List<RecommendationListDto> recommendationSummaries = createListRecommendationListDto(recommendationDtos);
        List<ReviewListDto> reviewSummaries = createListReviewListDto(reviewDtos);
        ServiceAddresses serviceAddresses = createServiceAddresses(productDto, recommendationDtos, reviewDtos, serviceAddress);
        return createProductListDto(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }

    private List<RecommendationListDto> createListRecommendationListDto(List<RecommendationDto> recommendationDtos) {
        return (recommendationDtos == null) ? null :
                recommendationDtos.stream()
                        .map(r -> new RecommendationListDto()
                                .setRecommendationId(r.getRecommendationId())
                                .setAuthor(r.getAuthor())
                                .setRate(r.getRate())
                                .setContent(r.getContent()))
                        .collect(Collectors.toList());
    }

    private List<ReviewListDto> createListReviewListDto(List<ReviewDto> reviewDtos) {
        return (reviewDtos == null) ? null :
                reviewDtos.stream()
                        .map(r -> new ReviewListDto()
                                .setReviewId(r.getReviewId())
                                .setAuthor(r.getAuthor())
                                .setSubject(r.getSubject())
                                .setContent(r.getContent()))
                        .collect(Collectors.toList());
    }

    private ServiceAddresses createServiceAddresses(ProductDto productDto, List<RecommendationDto> recommendationDtos, List<ReviewDto> reviewDtos, String serviceAddress) {
        String productAddress = productDto.getServiceAddress();
        String reviewAddress = (reviewDtos != null && reviewDtos.size() > 0) ? reviewDtos.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendationDtos != null && recommendationDtos.size() > 0) ? recommendationDtos.get(0).getServiceAddress() : "";
        return new ServiceAddresses()
                .setCmp(serviceAddress)
                .setPro(productAddress)
                .setRev(reviewAddress)
                .setRec(recommendationAddress);
    }

    private ProductListDto createProductListDto(int productId, String name, int weight, List<RecommendationListDto> recommendationSummaries, List<ReviewListDto> reviewSummaries, ServiceAddresses serviceAddresses) {
        return new ProductListDto()
                .setProductId(productId)
                .setName(name)
                .setWeight(weight)
                .setRecommendations(recommendationSummaries)
                .setReviews(reviewSummaries)
                .setServiceAddresses(serviceAddresses);
    }
}