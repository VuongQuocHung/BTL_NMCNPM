package dao;

import model.Role;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

public class RoleDao extends BaseDao<Role> {

    public RoleDao() {
        super(Role.class);
    }

    public Optional<Role> findByName(String name) {
        try (Session session = openSession()) {
            Query<Role> q = session.createQuery("FROM Role r WHERE r.name = :name", Role.class);
            q.setParameter("name", name);
            return q.uniqueResultOptional();
        }
    }
}
