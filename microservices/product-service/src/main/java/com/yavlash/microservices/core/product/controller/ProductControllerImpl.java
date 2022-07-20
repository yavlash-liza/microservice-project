package com.yavlash.microservices.core.product.controller;

import com.yavlash.api.controller.ProductController;
import com.yavlash.api.dto.ProductDto;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.api.exceptions.NotFoundException;
import com.yavlash.microservices.core.product.entity.Product;
import com.yavlash.microservices.core.product.repository.ProductRepository;
import com.yavlash.microservices.core.product.services.ProductMapper;
import com.yavlash.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@Slf4j
@RestController
public class ProductControllerImpl implements ProductController {
    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductControllerImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<ProductDto> createProduct(ProductDto body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        Product entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log(log.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<ProductDto> getProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.info("Will get product info for id={}", productId);
        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(log.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId).log(log.getName(), FINE).map(e -> repository.delete(e)).flatMap(e -> e);
    }

    private ProductDto setServiceAddress(ProductDto e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}