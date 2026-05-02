package controller;

import model.dto.ReviewResponse;
import util.ServiceLocator;
import model.Review;
import util.ReviewService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewServlet extends BaseServlet {
    private ReviewService reviewService;
    @Override public void init() { reviewService = ServiceLocator.getInstance().reviewService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, reviewService.getReviewById(id)); return; }
        Long productId = ServletUtil.getLongParam(req, "productId");
        Long userId = ServletUtil.getLongParam(req, "userId");
        String ratingStr = ServletUtil.getStringParam(req, "rating");
        Integer rating = ratingStr != null ? Integer.parseInt(ratingStr) : null;
        int page = ServletUtil.getIntParam(req, "page", 0);
        int size = ServletUtil.getIntParam(req, "size", 10);
        String sortBy = ServletUtil.getStringParam(req, "sortBy");
        String sortDir = ServletUtil.getStringParam(req, "sortDir");
        List<ReviewResponse> responses = reviewService.getFilteredReviews(productId, userId, rating, sortBy, sortDir)
                .stream().map(ReviewResponse::from).collect(Collectors.toList());
        JsonUtil.writeJson(resp, 200, PageResponse.of(responses, page, size));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        requireAuth(req);
        Review review = JsonUtil.readBody(req, Review.class);
        JsonUtil.writeJson(resp, 200, reviewService.createReview(review, ServletUtil.getCurrentUserId(req)));
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        requireAuth(req);
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        JsonUtil.writeJson(resp, 200, reviewService.updateReview(id, JsonUtil.readBody(req, Review.class), ServletUtil.getCurrentUserId(req)));
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        requireAuth(req);
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        reviewService.deleteReview(id, ServletUtil.getCurrentUserId(req), ServletUtil.isAdmin(req));
        resp.setStatus(204);
    }
}
