package com.yavlash.api.controller;

import com.yavlash.api.dto.ProductDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface ProductController {
    Mono<ProductDto> createProduct(ProductDto body);

    @GetMapping(value = "/product/{productId}")
    Mono<ProductDto> getProduct(@PathVariable int productId);

    Mono<Void> deleteProduct(int productId);
}