package com.yavlash.microservices.core.recommendation;

import com.yavlash.api.core.recommendation.Recommendation;
import com.yavlash.api.event.Event;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static com.yavlash.api.event.Event.Type.CREATE;
import static com.yavlash.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Recommendation>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void getRecommendationsByProductId() {
        //given
        int productId = 1;

        //when && then
        sendCreateRecommendationEvent(productId, 1);
        sendCreateRecommendationEvent(productId, 2);
        sendCreateRecommendationEvent(productId, 3);
        assertEquals(3, (long) repository.findByProductId(productId).count().block());
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

        //when && then
        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, (long) repository.count().block());
        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateRecommendationEvent(productId, recommendationId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", thrown.getMessage());
        assertEquals(1, (long) repository.count().block());
    }

    @Test
    void deleteRecommendations() {
        //given
        int productId = 1;
        int recommendationId = 1;

        //when && then
        sendCreateRecommendationEvent(productId, recommendationId);
        assertEquals(1, (long) repository.findByProductId(productId).count().block());
        sendDeleteRecommendationEvent(productId);
        assertEquals(0, (long) repository.findByProductId(productId).count().block());
        sendDeleteRecommendationEvent(productId);
    }

    @Test
    void getRecommendationsMissingParameter() {
        // given && when && then
        getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getRecommendationsInvalidParameter() {
        // given && when && then
        getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
        // given && when && then
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

    private void sendCreateRecommendationEvent(int productId, int recommendationId) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
        Event<Integer, Recommendation> event = new Event(CREATE, productId, recommendation);
        messageProcessor.accept(event);
    }

    private void sendDeleteRecommendationEvent(int productId) {
        Event<Integer, Recommendation> event = new Event(DELETE, productId, null);
        messageProcessor.accept(event);
    }
}