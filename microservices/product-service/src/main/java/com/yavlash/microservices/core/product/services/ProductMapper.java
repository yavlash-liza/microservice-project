package com.yavlash.microservices.core.product.services;

import com.yavlash.api.dto.ProductDto;
import com.yavlash.microservices.core.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "serviceAddress", ignore = true)
    ProductDto entityToApi(Product entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    Product apiToEntity(ProductDto api);
}