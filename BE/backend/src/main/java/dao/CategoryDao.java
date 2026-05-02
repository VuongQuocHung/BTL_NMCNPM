package dao;

import model.Category;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class CategoryDao extends BaseDao<Category> {

    public CategoryDao() {
        super(Category.class);
    }

    public List<Category> findFiltered(String name, String sortBy, String sortDir) {
        try (Session session = openSession()) {
            StringBuilder hql = new StringBuilder("FROM Category c WHERE 1=1");
            if (name != null) hql.append(" AND LOWER(c.name) LIKE :name");
            hql.append(" ORDER BY c.").append("name".equals(sortBy) ? "name" : "id")
               .append(" ").append("desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC");

            Query<Category> q = session.createQuery(hql.toString(), Category.class);
            if (name != null) q.setParameter("name", "%" + name.toLowerCase() + "%");
            return q.list();
        }
    }

    public Optional<Category> findByName(String name) {
        try (Session session = openSession()) {
            return session.createQuery("FROM Category WHERE name = :name", Category.class)
                    .setParameter("name", name)
                    .uniqueResultOptional();
        }
    }
}
