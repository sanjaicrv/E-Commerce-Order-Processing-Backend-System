package com.example.ecommerce.review;

import java.util.List;

public interface ReviewServiceInterface {
    ReviewDTO createReview(ReviewDTO reviewDTO);
    List<ReviewDTO> getReviewsByProductId(Long productId);
    ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO);
    void deleteReview(Long reviewId);
}
