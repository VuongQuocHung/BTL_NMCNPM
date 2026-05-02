package dao;

import model.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserDao extends BaseDao<User> {

    public UserDao() {
        super(User.class);
    }

    public Optional<User> findByEmail(String email) {
        try (Session session = openSession()) {
            Query<User> q = session.createQuery(
                    "FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email", User.class);
            q.setParameter("email", email);
            return q.uniqueResultOptional();
        }
    }

    public boolean existsByEmail(String email) {
        try (Session session = openSession()) {
            Query<Long> q = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            q.setParameter("email", email);
            return q.uniqueResult() > 0;
        }
    }

    public Optional<User> findByResetToken(String resetToken) {
        try (Session session = openSession()) {
            Query<User> q = session.createQuery(
                    "FROM User u LEFT JOIN FETCH u.role WHERE u.resetToken = :token", User.class);
            q.setParameter("token", resetToken);
            return q.uniqueResultOptional();
        }
    }

    public Optional<User> findByVerificationToken(String token) {
        try (Session session = openSession()) {
            Query<User> q = session.createQuery(
                    "FROM User u LEFT JOIN FETCH u.role WHERE u.verificationToken = :token", User.class);
            q.setParameter("token", token);
            return q.uniqueResultOptional();
        }
    }

    public List<User> findFiltered(String email, String fullName, String phone, Long roleId,
                                    int page, int size, String sortBy, String sortDir) {
        try (Session session = openSession()) {
            StringBuilder hql = new StringBuilder("FROM User u LEFT JOIN FETCH u.role WHERE 1=1");
            if (email != null) hql.append(" AND LOWER(u.email) LIKE :email");
            if (fullName != null) hql.append(" AND LOWER(u.fullName) LIKE :fullName");
            if (phone != null) hql.append(" AND u.phone LIKE :phone");
            if (roleId != null) hql.append(" AND u.role.id = :roleId");
            hql.append(" ORDER BY u.").append(safeSortField(sortBy)).append(" ").append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");

            Query<User> q = session.createQuery(hql.toString(), User.class);
            if (email != null) q.setParameter("email", "%" + email.toLowerCase() + "%");
            if (fullName != null) q.setParameter("fullName", "%" + fullName.toLowerCase() + "%");
            if (phone != null) q.setParameter("phone", "%" + phone + "%");
            if (roleId != null) q.setParameter("roleId", roleId);

            return q.list();
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = openSession()) {
            return session.createQuery("FROM User u LEFT JOIN FETCH u.role", User.class).list();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = openSession()) {
            Query<User> q = session.createQuery(
                    "FROM User u LEFT JOIN FETCH u.role WHERE u.id = :id", User.class);
            q.setParameter("id", id);
            return q.uniqueResultOptional();
        }
    }

    private String safeSortField(String field) {
        if (field == null) return "id";
        return switch (field) {
            case "email", "fullName", "phone", "createdAt" -> field;
            default -> "id";
        };
    }
}
