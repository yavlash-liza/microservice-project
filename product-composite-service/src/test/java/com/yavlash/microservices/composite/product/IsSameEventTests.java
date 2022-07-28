package com.yavlash.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yavlash.api.dto.ProductDto;
import com.yavlash.api.event.Event;
import org.junit.jupiter.api.Test;

import static com.yavlash.api.event.Event.Type.CREATE;
import static com.yavlash.api.event.Event.Type.DELETE;
import static com.yavlash.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class IsSameEventTests {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {
        //given
        String name = "name";
        Event<Integer, ProductDto> event1 = new Event()
                .setEventType(CREATE)
                .setKey(1)
                .setData(new ProductDto().setProductId(1).setName(name).setWeight(1).setServiceAddress(null));
        Event<Integer, ProductDto> event2 = new Event()
                .setEventType(CREATE)
                .setKey(1)
                .setData(new ProductDto().setProductId(1).setName(name).setWeight(1).setServiceAddress(null));
        Event<Integer, ProductDto> event3 = new Event()
                .setEventType(DELETE)
                .setKey(1)
                .setData(null);
        Event<Integer, ProductDto> event4 = new Event()
                .setEventType(CREATE)
                .setKey(1)
                .setData(new ProductDto().setProductId(2).setName(name).setWeight(1).setServiceAddress(null));

        //when
        String event1Json = mapper.writeValueAsString(event1);

        //then
        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}