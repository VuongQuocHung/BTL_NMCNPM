package dao;

import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base DAO cung cấp các thao tác CRUD generic với Hibernate.
 */
public abstract class BaseDao<T> {

    private final Class<T> entityClass;

    protected BaseDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session openSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }

    /** Thực thi trong transaction, trả về kết quả */
    protected <R> R inTransaction(Function<Session, R> work) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            R result = work.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /** Thực thi trong transaction, không trả về kết quả */
    protected void inTransactionVoid(Consumer<Session> work) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            work.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    public Optional<T> findById(Long id) {
        try (Session session = openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        }
    }

    public List<T> findAll() {
        try (Session session = openSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    public T save(T entity) {
        return inTransaction(session -> {
            session.persist(entity);
            return entity;
        });
    }

    public T merge(T entity) {
        return inTransaction(session -> session.merge(entity));
    }

    public void delete(T entity) {
        inTransactionVoid(session -> {
            T managed = session.merge(entity);
            session.remove(managed);
        });
    }

    public void deleteById(Long id) {
        inTransactionVoid(session -> {
            T entity = session.get(entityClass, id);
            if (entity != null) {
                session.remove(entity);
            }
        });
    }
}
