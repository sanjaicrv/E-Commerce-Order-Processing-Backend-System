package com.example.ecommerce.review;

import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.exception.ReviewNotFoundException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.product.ProductEntity;
import com.example.ecommerce.product.ProductJpa;
import com.example.ecommerce.user.UserEntity;
import com.example.ecommerce.user.UserJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService implements ReviewServiceInterface {

    @Autowired
    private ReviewJpa reviewJpa;

    @Autowired
    private UserJpa userJpa;

    @Autowired
    private ProductJpa productJpa;

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        UserEntity user = userJpa.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + reviewDTO.getUserId()));

        ProductEntity product = productJpa.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + reviewDTO.getProductId()));

        ReviewEntity review = ReviewEntity.builder()
                .user(user)
                .product(product)
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .build();

        ReviewEntity saved = reviewJpa.save(review);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByProductId(Long productId) {
        if (!productJpa.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }
        return reviewJpa.findByProductProductId(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO) {
        ReviewEntity review = reviewJpa.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        ReviewEntity updated = reviewJpa.save(review);
        return convertToDTO(updated);
    }

    @Override
    public void deleteReview(Long reviewId) {
        ReviewEntity review = reviewJpa.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        reviewJpa.delete(review);
    }

    private ReviewDTO convertToDTO(ReviewEntity entity) {
        return ReviewDTO.builder()
                .reviewId(entity.getReviewId())
                .userId(entity.getUser().getUserId())
                .productId(entity.getProduct().getProductId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .build();
    }
}
