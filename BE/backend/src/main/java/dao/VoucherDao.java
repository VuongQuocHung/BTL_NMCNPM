package dao;

import model.Voucher;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class VoucherDao extends BaseDao<Voucher> {
    public VoucherDao() { super(Voucher.class); }

    public Optional<Voucher> findByCodeIgnoreCase(String code) {
        try (Session s = openSession()) {
            return s.createQuery("FROM Voucher v WHERE LOWER(v.code) = :code", Voucher.class)
                    .setParameter("code", code.toLowerCase()).uniqueResultOptional();
        }
    }

    public Optional<Voucher> getVoucherByCode(String code) {
        return findByCodeIgnoreCase(code);
    }

    public Optional<Voucher> getVoucherById(Long voucherId) {
        return findById(voucherId);
    }

    public boolean existsByCodeIgnoreCase(String code) {
        try (Session s = openSession()) {
            Long count = s.createQuery("SELECT COUNT(v) FROM Voucher v WHERE LOWER(v.code) = :code", Long.class)
                    .setParameter("code", code.toLowerCase()).uniqueResult();
            return count != null && count > 0;
        }
    }

    public List<Voucher> findFiltered(String code, Boolean active, String sortBy, String sortDir) {
        try (Session s = openSession()) {
            StringBuilder hql = new StringBuilder("FROM Voucher v WHERE 1=1");
            if (code != null) hql.append(" AND LOWER(v.code) LIKE :code");
            if (active != null) hql.append(" AND v.active = :active");
            hql.append(" ORDER BY v.").append("code".equals(sortBy) ? "code" : "id")
               .append(" ").append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");
            Query<Voucher> q = s.createQuery(hql.toString(), Voucher.class);
            if (code != null) q.setParameter("code", "%" + code.toLowerCase() + "%");
            if (active != null) q.setParameter("active", active);
            return q.list();
        }
    }

    public List<Voucher> findActiveVouchers(String sortBy, String sortDir) {
        try (Session s = openSession()) {
            LocalDateTime now = LocalDateTime.now();
            String hql = "FROM Voucher v WHERE v.active = true " +
                    "AND (v.startDate IS NULL OR v.startDate <= :now) " +
                    "AND (v.endDate IS NULL OR v.endDate >= :now) " +
                    "AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit) " +
                    "ORDER BY v." + ("code".equals(sortBy) ? "code" : "id") + " " +
                    ("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");
            return s.createQuery(hql, Voucher.class).setParameter("now", now).list();
        }
    }

    /**
     * Ten ham theo so do thiet ke: VoucherDAO.getVouchersForCustomer(userId).
     * He thong hien chua gan voucher theo tung khach, nen tra ve cac voucher dang kha dung.
     */
    public List<Voucher> getVouchersForCustomer(Long userId) {
        return findActiveVouchers("id", "desc");
    }
}
