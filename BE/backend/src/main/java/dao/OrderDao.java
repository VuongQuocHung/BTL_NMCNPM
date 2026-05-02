package dao;

import model.Order;
import model.OrderStatus;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class OrderDao extends BaseDao<Order> {

    public OrderDao() {
        super(Order.class);
    }

    public Optional<Order> findWithDetailsById(Long id) {
        try (Session session = openSession()) {
            Query<Order> q = session.createQuery(
                    "FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product p " +
                    "LEFT JOIN FETCH p.images LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
                    "LEFT JOIN FETCH p.specification LEFT JOIN FETCH o.user " +
                    "WHERE o.id = :id", Order.class);
            q.setParameter("id", id);
            return q.uniqueResultOptional();
        }
    }

    public List<Order> findByUserId(Long userId) {
        try (Session session = openSession()) {
            Query<Order> q = session.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category LEFT JOIN FETCH p.specification WHERE o.user.id = :userId", Order.class);
            q.setParameter("userId", userId);
            return q.list();
        }
    }

    public List<Order> findFiltered(OrderStatus status, Long userId, String phoneNumber,
                                     BigDecimal minAmount, BigDecimal maxAmount,
                                     int page, int size, String sortBy, String sortDir) {
        try (Session session = openSession()) {
            StringBuilder hql = new StringBuilder(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category LEFT JOIN FETCH p.specification WHERE 1=1");
            if (status != null) hql.append(" AND o.status = :status");
            if (userId != null) hql.append(" AND o.user.id = :userId");
            if (phoneNumber != null) hql.append(" AND o.phoneNumber LIKE :phoneNumber");
            if (minAmount != null) hql.append(" AND o.totalAmount >= :minAmount");
            if (maxAmount != null) hql.append(" AND o.totalAmount <= :maxAmount");
            hql.append(" ORDER BY o.").append(safeSortField(sortBy)).append(" ")
               .append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");

            Query<Order> q = session.createQuery(hql.toString(), Order.class);
            if (status != null) q.setParameter("status", status);
            if (userId != null) q.setParameter("userId", userId);
            if (phoneNumber != null) q.setParameter("phoneNumber", "%" + phoneNumber + "%");
            if (minAmount != null) q.setParameter("minAmount", minAmount);
            if (maxAmount != null) q.setParameter("maxAmount", maxAmount);

            return q.list();
        }
    }

    private String safeSortField(String field) {
        if (field == null) return "id";
        return switch (field) {
            case "orderDate", "totalAmount", "status" -> field;
            default -> "id";
        };
    }
}
