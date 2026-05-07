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
                    "SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product p " +
                    "LEFT JOIN FETCH p.images LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
                    "WHERE c.user.id = :userId", Cart.class);
            q.setParameter("userId", userId);
            return q.uniqueResultOptional();
        }
    }

    /**
     * Ten ham theo so do thiet ke: CartDAO.getCartByUserId(userId).
     */
    public Optional<Cart> getCartByUserId(Long userId) {
        return findWithItemsByUserId(userId);
    }

    /**
     * Ten ham theo so do thiet ke: CartDAO.saveOrUpdateCart(cart).
     */
    public Cart saveOrUpdateCart(Cart cart) {
        return merge(cart);
    }

    public void removeItem(Long cartId, Long productId) {
        inTransactionVoid(session -> session.createMutationQuery(
                        "DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
                .setParameter("cartId", cartId)
                .setParameter("productId", productId)
                .executeUpdate());
    }

    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        inTransactionVoid(session -> session.createMutationQuery(
                        "UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
                .setParameter("quantity", quantity)
                .setParameter("cartId", cartId)
                .setParameter("productId", productId)
                .executeUpdate());
    }
}
