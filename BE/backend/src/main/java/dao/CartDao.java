package dao;

import model.Cart;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

public class CartDao extends BaseDao<Cart> {

    public CartDao() {
        super(Cart.class);
    }

    public Optional<Cart> findByUserId(Long userId) {
        try (Session session = openSession()) {
            Query<Cart> q = session.createQuery(
                    "FROM Cart c WHERE c.user.id = :userId", Cart.class);
            q.setParameter("userId", userId);
            return q.uniqueResultOptional();
        }
    }

    public Optional<Cart> findWithItemsByUserId(Long userId) {
        try (Session session = openSession()) {
            Query<Cart> q = session.createQuery(
                    "FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product p " +
                    "LEFT JOIN FETCH p.images LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
                    "WHERE c.user.id = :userId", Cart.class);
            q.setParameter("userId", userId);
            return q.uniqueResultOptional();
        }
    }
}
