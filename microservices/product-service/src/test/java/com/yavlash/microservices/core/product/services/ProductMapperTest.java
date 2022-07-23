package com.yavlash.microservices.core.product.services;

import com.yavlash.api.dto.ProductDto;
import com.yavlash.microservices.core.product.entity.Product;
import com.yavlash.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        //given
        ProductDto api = new ProductDto()
                .setProductId(1)
                .setName("n")
                .setWeight(1)
                .setServiceAddress("sa");
        Product entity = mapper.fromDto(api);
        ProductDto api2 = mapper.toDto(entity);

        //when && then
        assertNotNull(mapper);
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getWeight(), api2.getWeight());
        assertNull(api2.getServiceAddress());
    }
}