package com.yavlash.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import com.yavlash.api.core.recommendation.Recommendation;
import com.yavlash.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getRecommendationsByProductId() {
        //given
        int productId = 1;

        //when
        postAndVerifyRecommendation(productId, 1, OK);
        postAndVerifyRecommendation(productId, 2, OK);
        postAndVerifyRecommendation(productId, 3, OK);

        //then
        assertEquals(3, repository.findByProductId(productId).size());
        getAndVerifyRecommendationsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        //given
        int productId = 1;
        int recommendationId = 1;

        //when
        postAndVerifyRecommendation(productId, recommendationId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.recommendationId").isEqualTo(recommendationId);

        //then
        assertEquals(1, repository.count());
        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");
        assertEquals(1, repository.count());
    }

    @Test
    void deleteRecommendations() {
        //given
        int productId = 1;
        int recommendationId = 1;

        //when && then
        postAndVerifyRecommendation(productId, recommendationId, OK);
        assertEquals(1, repository.findByProductId(productId).size());
        deleteAndVerifyRecommendationsByProductId(productId, OK);
        assertEquals(0, repository.findByProductId(productId).size());
        deleteAndVerifyRecommendationsByProductId(productId, OK);
    }

    @Test
    void getRecommendationsMissingParameter() {
        //given && when && then
        getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter() {
        //given && when && then
        getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
        //given && when && then
        getAndVerifyRecommendationsByProductId("?productId=113", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {
        //given
        int productIdInvalid = -1;

        //when && then
        getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/recommendation" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
        return client.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/recommendation?productId=" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}