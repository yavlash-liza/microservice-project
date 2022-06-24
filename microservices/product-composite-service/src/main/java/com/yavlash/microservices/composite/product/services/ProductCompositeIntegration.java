package com.yavlash.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yavlash.api.core.product.Product;
import com.yavlash.api.core.product.ProductService;
import com.yavlash.api.core.recommendation.Recommendation;
import com.yavlash.api.core.recommendation.RecommendationService;
import com.yavlash.api.core.review.Review;
import com.yavlash.api.core.review.ReviewService;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.api.exceptions.NotFoundException;
import com.yavlash.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-service.host}")
                    String productServiceHost,
            @Value("${app.product-service.port}")
                    int productServicePort,
            @Value("${app.recommendation-service.host}")
                    String recommendationServiceHost,
            @Value("${app.recommendation-service.port}")
                    int recommendationServicePort,
            @Value("${app.review-service.host}")
                    String reviewServiceHost,
            @Value("${app.review-service.port}")
                    int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }

    @Override
    public Product createProduct(Product body) {
        try {
            String url = productServiceUrl;
            LOG.debug("Will post a new product to URL: {}", url);
            Product product = restTemplate.postForObject(url, body, Product.class);
            if (product != null) {
                LOG.debug("Created a product with id: {}", product.getProductId());
            }
            return product;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call the getProduct API on URL: {}", url);
            Product product = restTemplate.getForObject(url, Product.class);
            if (product != null) {
                LOG.debug("Found a product with id: {}", product.getProductId());
            }
            return product;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call the deleteProduct API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            String url = recommendationServiceUrl;
            LOG.debug("Will post a new recommendation to URL: {}", url);
            Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
            if (recommendation != null) {
                LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());
            }
            return recommendation;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
                    })
                    .getBody();
            if (recommendations != null) {
                LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            }
            return recommendations;
        } catch (Exception e) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteRecommendations API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public Review createReview(Review body) {
        try {
            String url = reviewServiceUrl;
            LOG.debug("Will post a new review to URL: {}", url);
            Review review = restTemplate.postForObject(url, body, Review.class);
            if (review != null) {
                LOG.debug("Created a review with id: {}", review.getProductId());
            }
            return review;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
                    })
                    .getBody();
            if (reviews != null) {
                LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            }
            return reviews;
        } catch (Exception e) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;
            LOG.debug("Will call the deleteReviews API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException e) {
        switch (e.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(e));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(e));
            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", e.getStatusCode());
                LOG.warn("Error body: {}", e.getResponseBodyAsString());
                return e;
        }
    }

    private String getErrorMessage(HttpClientErrorException e) {
        try {
            return mapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return e.getMessage();
        }
    }
}