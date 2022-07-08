package com.yavlash.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yavlash.api.core.product.Product;
import com.yavlash.api.event.Event;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.microservices.core.product.persistence.ProductRepository;

import static com.yavlash.api.event.Event.Type.CREATE;
import static com.yavlash.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"eureka.client.enabled=false"})
class ProductServiceApplicationTests extends MongoDbTestBase {
    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Product>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void getProductById() {
        //given
        int productId = 1;

        //when && then
        assertNull(repository.findByProductId(productId).block());
        assertEquals(0, (long) repository.count().block());
        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());
        assertEquals(1, (long) repository.count().block());
        getAndVerifyProduct(productId, OK)
                .jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    void duplicateError() {
        //given
        int productId = 1;

        //when && then
        assertNull(repository.findByProductId(productId).block());
        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());
        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateProductEvent(productId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());
    }

    @Test
    void deleteProduct() {
        //given
        int productId = 1;

        //when && then
        sendCreateProductEvent(productId);
        assertNotNull(repository.findByProductId(productId).block());
        sendDeleteProductEvent(productId);
        assertNull(repository.findByProductId(productId).block());
        sendDeleteProductEvent(productId);
    }

    @Test
    void getProductInvalidParameterString() {
        //given && when && then
        getAndVerifyProduct("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getProductNotFound() {
        //given
        int productIdNotFound = 13;

        //when && then
        getAndVerifyProduct(productIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {
        //given
        int productIdInvalid = -1;

        //when && then
        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateProductEvent(int productId) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        Event<Integer, Product> event = new Event(CREATE, productId, product);
        messageProcessor.accept(event);
    }

    private void sendDeleteProductEvent(int productId) {
        Event<Integer, Product> event = new Event(DELETE, productId, null);
        messageProcessor.accept(event);
    }
}