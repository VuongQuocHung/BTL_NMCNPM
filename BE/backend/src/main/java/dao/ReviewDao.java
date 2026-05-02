package dao;

import model.Review;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class ReviewDao extends BaseDao<Review> {
    public ReviewDao() { super(Review.class); }

    public List<Review> findByProductId(Long productId) {
        try (Session s = openSession()) {
            return s.createQuery("FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.product WHERE r.product.id = :pid", Review.class)
                    .setParameter("pid", productId).list();
        }
    }

    public List<Review> findByUserId(Long userId) {
        try (Session s = openSession()) {
            return s.createQuery("FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.product WHERE r.user.id = :uid", Review.class)
                    .setParameter("uid", userId).list();
        }
    }

    public List<Review> findFiltered(Long productId, Long userId, Integer rating, String sortBy, String sortDir) {
        try (Session s = openSession()) {
            StringBuilder hql = new StringBuilder("FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.product WHERE 1=1");
            if (productId != null) hql.append(" AND r.product.id = :productId");
            if (userId != null) hql.append(" AND r.user.id = :userId");
            if (rating != null) hql.append(" AND r.rating = :rating");
            hql.append(" ORDER BY r.").append("createdAt".equals(sortBy) ? "createdAt" : "id")
               .append(" ").append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");
            Query<Review> q = s.createQuery(hql.toString(), Review.class);
            if (productId != null) q.setParameter("productId", productId);
            if (userId != null) q.setParameter("userId", userId);
            if (rating != null) q.setParameter("rating", rating);
            return q.list();
        }
    }

    @Override
    public Optional<Review> findById(Long id) {
        try (Session s = openSession()) {
            return s.createQuery("FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.product WHERE r.id = :id", Review.class)
                    .setParameter("id", id).uniqueResultOptional();
        }
    }
}
