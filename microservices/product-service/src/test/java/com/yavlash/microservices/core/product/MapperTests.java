package com.yavlash.microservices.core.product;

import com.yavlash.api.core.product.Product;
import com.yavlash.microservices.core.product.persistence.ProductEntity;
import com.yavlash.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        //given
        Product api = new Product(1, "n", 1, "sa");

        //when && then
        assertNotNull(mapper);
        ProductEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());
        Product api2 = mapper.entityToApi(entity);
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(),      api2.getName());
        assertEquals(api.getWeight(),    api2.getWeight());
        assertNull(api2.getServiceAddress());
    }
}