package dao;

import model.Brand;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class BrandDao extends BaseDao<Brand> {

    public BrandDao() {
        super(Brand.class);
    }

    public List<Brand> findFiltered(String name, String sortBy, String sortDir) {
        try (Session session = openSession()) {
            StringBuilder hql = new StringBuilder("FROM Brand b WHERE 1=1");
            if (name != null) hql.append(" AND LOWER(b.name) LIKE :name");
            hql.append(" ORDER BY b.").append("name".equals(sortBy) ? "name" : "id")
               .append(" ").append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");

            Query<Brand> q = session.createQuery(hql.toString(), Brand.class);
            if (name != null) q.setParameter("name", "%" + name.toLowerCase() + "%");
            return q.list();
        }
    }

    public Optional<Brand> findByName(String name) {
        try (Session session = openSession()) {
            return session.createQuery("FROM Brand WHERE name = :name", Brand.class)
                    .setParameter("name", name)
                    .uniqueResultOptional();
        }
    }
}
