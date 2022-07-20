package com.yavlash.microservices.core.review.services;

import com.yavlash.api.dto.ReviewDto;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.microservices.core.review.entity.Review;
import com.yavlash.microservices.core.review.repository.ReviewRepository;
import com.yavlash.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ReviewMapper mapper, ReviewRepository repository, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    public ReviewDto createReview(ReviewDto reviewDto) {
        try {
            Review entity = mapper.apiToEntity(reviewDto);
            Review newEntity = repository.save(entity);
            log.debug("createReview: created a review entity: {}/{}", reviewDto.getProductId(), reviewDto.getReviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + reviewDto.getProductId() + ", Review Id:" + reviewDto.getReviewId());
        }
    }

    public List<ReviewDto> findReviewsByProductId(int productId) {
        List<Review> entityList = repository.findByProductId(productId);
        List<ReviewDto> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
        log.debug("Response size: {}", list.size());
        return list;
    }

    public void deleteReviewsByProductId(int productId) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}