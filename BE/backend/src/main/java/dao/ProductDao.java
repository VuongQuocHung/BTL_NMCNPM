package dao;

import model.Product;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductDao extends BaseDao<Product> {

    public ProductDao() {
        super(Product.class);
    }

    public void restoreStock(int productId, int quantity) {
        inTransactionVoid(session -> {
            session.createNativeQuery("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
                    .setParameter("quantity", quantity)
                    .setParameter("productId", productId)
                    .executeUpdate();
        });
    }

    public Optional<Product> findWithDetailsById(Long id) {
        try (Session session = openSession()) {
            Query<Product> q = session.createQuery(
                    "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.specification " +
                    "LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category WHERE p.id = :id", Product.class);
            q.setParameter("id", id);
            return q.uniqueResultOptional();
        }
    }

    /**
     * Ten ham theo so do thiet ke: ProductDAO.getProductById(productId).
     * Lay san pham kem anh, cau hinh, thuong hieu va danh muc de hien thi gio hang.
     */
    public Optional<Product> getProductById(Long productId) {
        return findWithDetailsById(productId);
    }

    /**
     * Ten ham theo so do thiet ke: ProductDAO.checkStock(productId, quantity).
     * Tra ve true khi san pham con ton tai va so luong yeu cau khong vuot ton kho.
     */
    public boolean checkStock(Long productId, int quantity) {
        return getStock(productId) >= quantity;
    }

    public int getStock(Long productId) {
        try (Session session = openSession()) {
            Integer stock = session.createQuery(
                            "SELECT p.stock FROM Product p WHERE p.id = :productId", Integer.class)
                    .setParameter("productId", productId)
                    .uniqueResult();
            return stock != null ? stock : 0;
        }
    }

    public List<Product> findFiltered(String name, Long brandId, Long categoryId,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       int page, int size, String sortBy, String sortDir) {
        try (Session session = openSession()) {
            StringBuilder hql = new StringBuilder(
                    "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.specification " +
                    "LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category WHERE 1=1");
            if (name != null) hql.append(" AND LOWER(p.name) LIKE :name");
            if (brandId != null) hql.append(" AND p.brand.id = :brandId");
            if (categoryId != null) hql.append(" AND p.category.id = :categoryId");
            if (minPrice != null) hql.append(" AND p.price >= :minPrice");
            if (maxPrice != null) hql.append(" AND p.price <= :maxPrice");
            hql.append(" ORDER BY p.").append(safeSortField(sortBy)).append(" ").append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");

            Query<Product> q = session.createQuery(hql.toString(), Product.class);
            if (name != null) q.setParameter("name", "%" + name.toLowerCase() + "%");
            if (brandId != null) q.setParameter("brandId", brandId);
            if (categoryId != null) q.setParameter("categoryId", categoryId);
            if (minPrice != null) q.setParameter("minPrice", minPrice);
            if (maxPrice != null) q.setParameter("maxPrice", maxPrice);

            return q.list();
        }
    }

    public List<Product> findByIdsWithDetails(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        try (Session session = openSession()) {
            Query<Product> q = session.createQuery(
                    "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.specification " +
                    "LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category WHERE p.id IN :ids", Product.class);
            q.setParameter("ids", ids);
            return q.list();
        }
    }

    @Override
    public List<Product> findAll() {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.specification " +
                    "LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category", Product.class).list();
        }
    }

    private String safeSortField(String field) {
        if (field == null) return "id";
        return switch (field) {
            case "name", "price", "stock", "createdAt", "updatedAt" -> field;
            default -> "id";
        };
    }
}