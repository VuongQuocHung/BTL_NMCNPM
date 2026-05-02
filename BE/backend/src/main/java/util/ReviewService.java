package util;

import dao.ReviewDao;
import model.dto.ReviewResponse;
import model.Review;
import model.User;
import util.ApiException;
import java.util.List;

public class ReviewService {
    private final ReviewDao reviewDao;
    public ReviewService(ReviewDao reviewDao) { this.reviewDao = reviewDao; }

    public List<Review> getFilteredReviews(Long productId, Long userId, Integer rating, String sortBy, String sortDir) {
        return reviewDao.findFiltered(productId, userId, rating, sortBy, sortDir);
    }

    public Review getReviewEntityById(Long id) {
        return reviewDao.findById(id).orElseThrow(() -> ApiException.notFound("Review not found"));
    }

    public ReviewResponse getReviewById(Long id) {
        return ReviewResponse.from(getReviewEntityById(id));
    }

    public ReviewResponse createReview(Review review, Long currentUserId) {
        User user = new User(); user.setId(currentUserId);
        review.setUser(user);
        return ReviewResponse.from(reviewDao.save(review));
    }

    public ReviewResponse updateReview(Long id, Review details, Long currentUserId) {
        Review review = getReviewEntityById(id);
        if (!review.getUser().getId().equals(currentUserId)) throw ApiException.forbidden("Bạn không có quyền sửa đánh giá này");
        review.setRating(details.getRating());
        review.setComment(details.getComment());
        return ReviewResponse.from(reviewDao.merge(review));
    }

    public void deleteReview(Long id, Long currentUserId, boolean isAdmin) {
        Review review = getReviewEntityById(id);
        if (!isAdmin && !review.getUser().getId().equals(currentUserId)) throw ApiException.forbidden("Bạn không có quyền xóa đánh giá này");
        reviewDao.delete(review);
    }
}