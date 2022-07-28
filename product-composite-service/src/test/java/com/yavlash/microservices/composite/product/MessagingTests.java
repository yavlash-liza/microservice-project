package com.yavlash.microservices.composite.product;

import com.yavlash.api.dto.ProductDto;
import com.yavlash.api.dto.ProductListDto;
import com.yavlash.api.dto.RecommendationDto;
import com.yavlash.api.dto.RecommendationListDto;
import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.dto.ReviewListDto;
import com.yavlash.api.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static com.yavlash.api.event.Event.Type.CREATE;
import static com.yavlash.api.event.Event.Type.DELETE;
import static com.yavlash.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(
        webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "eureka.client.enabled=false"})
@Import({TestChannelBinderConfiguration.class})
class MessagingTests {
    private static final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp() {
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProduct1() {
        //given
        ProductListDto composite = new ProductListDto()
                .setProductId(1)
                .setName("name")
                .setWeight(1)
                .setRecommendations(null)
                .setReviews(null)
                .setServiceAddresses(null);
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        //when && then
        assertEquals(1, productMessages.size());

        Event<Integer, ProductDto> expectedEvent = new Event()
                .setEventType(CREATE)
                .setKey(composite.getProductId())
                .setData(new ProductDto()
                        .setProductId(composite.getProductId())
                        .setName(composite.getName())
                        .setWeight(composite.getWeight())
                        .setServiceAddress(null));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));

        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewMessages.size());
    }

    @Test
    void createCompositeProduct2() {
        //given
        RecommendationListDto recommendationListDto = new RecommendationListDto().setRecommendationId(1).setAuthor("a").setRate(1).setContent("c");
        ReviewListDto reviewListDto = new ReviewListDto().setReviewId(1).setAuthor("a").setSubject("s").setContent("c");
        ProductListDto composite = new ProductListDto()
                .setProductId(1)
                .setName("name")
                .setWeight(1)
                .setRecommendations(singletonList(recommendationListDto))
                .setReviews(singletonList(reviewListDto))
                .setServiceAddresses(null);
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        //when && then
        assertEquals(1, productMessages.size());

        Event<Integer, ProductDto> expectedProductEvent = new Event()
                .setEventType(CREATE)
                .setKey(composite.getProductId())
                .setData(new ProductDto()
                        .setProductId(composite.getProductId())
                        .setName(composite.getName())
                        .setWeight(composite.getWeight())
                        .setServiceAddress(null));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));
        assertEquals(1, recommendationMessages.size());

        RecommendationListDto rec = composite.getRecommendations().get(0);
        Event<Integer, ProductDto> expectedRecommendationEvent = new Event()
                .setEventType(CREATE)
                .setKey(composite.getProductId())
                .setData(new RecommendationDto()
                        .setProductId(composite.getProductId())
                        .setRecommendationId(rec.getRecommendationId())
                        .setAuthor(rec.getAuthor())
                        .setRate(rec.getRate())
                        .setContent(rec.getContent())
                        .setServiceAddress(null));
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, reviewMessages.size());

        ReviewListDto rev = composite.getReviews().get(0);
        Event<Integer, ProductDto> expectedReviewEvent = new Event()
                .setEventType(CREATE)
                .setKey(composite.getProductId())
                .setData(new ReviewDto()
                        .setProductId(composite.getProductId())
                        .setReviewId(rev.getReviewId())
                        .setAuthor(rev.getAuthor())
                        .setSubject(rev.getSubject())
                        .setContent(rev.getContent())
                        .setServiceAddress(null));
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        assertEquals(1, productMessages.size());

        Event<Integer, ProductDto> expectedProductEvent = new Event()
                .setEventType(DELETE)
                .setKey(1)
                .setData(null);
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

        assertEquals(1, recommendationMessages.size());

        Event<Integer, ProductDto> expectedRecommendationEvent = new Event()
                .setEventType(DELETE)
                .setKey(1)
                .setData(null);
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        assertEquals(1, reviewMessages.size());

        Event<Integer, ProductDto> expectedReviewEvent = new Event()
                .setEventType(DELETE)
                .setKey(1)
                .setData(null);
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;
        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);
            if (message == null) {
                anyMoreMessages = false;
            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException e) {
            LOG.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }

    private void postAndVerifyProduct(ProductListDto compositeProduct, HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(just(compositeProduct), ProductListDto.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}