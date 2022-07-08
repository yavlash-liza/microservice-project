package com.yavlash.microservices.core.review.services;

import com.yavlash.api.core.review.Review;
import com.yavlash.api.exceptions.InvalidInputException;
import com.yavlash.microservices.core.review.persistence.ReviewEntity;
import com.yavlash.microservices.core.review.persistence.ReviewRepository;
import com.yavlash.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Review createReview(Review review) {
        try {
            ReviewEntity entity = mapper.apiToEntity(review);
            ReviewEntity newEntity = repository.save(entity);
            LOG.debug("createReview: created a review entity: {}/{}", review.getProductId(), review.getReviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + review.getProductId() + ", Review Id:" + review.getReviewId());
        }
    }

    public List<Review> findReviewsByProductId(int productId) {
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));
        LOG.debug("Response size: {}", list.size());
        return list;
    }

    public void deleteReviewsByProductId(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}