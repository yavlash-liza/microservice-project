package com.yavlash.microservices.core.review;

import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.event.Event;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.microservices.core.review.repository.ReviewRepository;
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

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.cloud.stream.defaultBinder=rabbit",
        "logging.level.se.magnus=DEBUG",
        "eureka.client.enabled=false"})
class ReviewServiceApplicationTests extends MySqlTestBase {
    @Autowired
    private WebTestClient client;

    @Autowired
    private ReviewRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, ReviewDto>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getReviewsByProductId() {
        //given
        int productId = 1;

        //when && then
        assertEquals(0, repository.findByProductId(productId).size());
        sendCreateReviewEvent(productId, 1);
        sendCreateReviewEvent(productId, 2);
        sendCreateReviewEvent(productId, 3);
        assertEquals(3, repository.findByProductId(productId).size());
        getAndVerifyReviewsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].reviewId").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        //given
        int productId = 1;
        int reviewId = 1;

        //when && then
        assertEquals(0, repository.count());
        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.count());
        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateReviewEvent(productId, reviewId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: 1, Review Id:1", thrown.getMessage());
        assertEquals(1, repository.count());
    }

    @Test
    void deleteReviews() {
        //given
        int productId = 1;
        int reviewId = 1;

        //when && then
        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.findByProductId(productId).size());
        sendDeleteReviewEvent(productId);
        assertEquals(0, repository.findByProductId(productId).size());
        sendDeleteReviewEvent(productId);
    }

    @Test
    void getReviewsMissingParameter() {
        //given && when && then
        getAndVerifyReviewsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
    }

    @Test
    void getReviewsInvalidParameter() {
        //given && when && then
        getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {
        //given && when && then
        getAndVerifyReviewsByProductId("?productId=213", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {
        //given && when
        int productIdInvalid = -1;

        //then
        getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/review" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateReviewEvent(int productId, int reviewId) {
        ReviewDto reviewDto = new ReviewDto()
                .setProductId(productId)
                .setReviewId(reviewId)
                .setAuthor("Author " + reviewId)
                .setSubject("Subject " + reviewId)
                .setContent("Content " + reviewId)
                .setServiceAddress("SA");
        Event<Integer, ReviewDto> event = new Event(CREATE, productId, reviewDto);
        messageProcessor.accept(event);
    }

    private void sendDeleteReviewEvent(int productId) {
        Event<Integer, ReviewDto> event = new Event(DELETE, productId, null);
        messageProcessor.accept(event);
    }
}