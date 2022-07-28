package com.yavlash.microservices.core.product.repository;

import com.yavlash.microservices.core.product.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<Product, String> {
    Mono<Product> findByProductId(int productId);
}