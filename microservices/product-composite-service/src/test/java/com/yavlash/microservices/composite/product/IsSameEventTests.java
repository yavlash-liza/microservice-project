package com.yavlash.microservices.composite.product;

import static com.yavlash.api.event.Event.Type.CREATE;
import static com.yavlash.api.event.Event.Type.DELETE;
import static com.yavlash.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yavlash.api.core.product.Product;
import com.yavlash.api.event.Event;
import org.junit.jupiter.api.Test;

public class IsSameEventTests {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {
        //given
        Event<Integer, Product> event1 = new Event<>(CREATE, 1, new Product(1, "name", 1, null));
        Event<Integer, Product> event2 = new Event<>(CREATE, 1, new Product(1, "name", 1, null));
        Event<Integer, Product> event3 = new Event<>(DELETE, 1, null);
        Event<Integer, Product> event4 = new Event<>(CREATE, 1, new Product(2, "name", 1, null));

        //when
        String event1Json = mapper.writeValueAsString(event1);

        //then
        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}