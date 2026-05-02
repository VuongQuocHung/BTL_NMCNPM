package util;

import model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Singleton quản lý Hibernate SessionFactory.
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        Configuration cfg = new Configuration();

        // Database connection từ file cấu hình DatabaseConfig
        String dbUrl = DatabaseConfig.URL;
        String dbUser = DatabaseConfig.USERNAME;
        String dbPass = DatabaseConfig.PASSWORD;
        
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        cfg.setProperty("hibernate.connection.url", dbUrl);
        cfg.setProperty("hibernate.connection.username", dbUser);
        cfg.setProperty("hibernate.connection.password", dbPass);
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        // Hibernate settings
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");
        cfg.setProperty("hibernate.show_sql", "true");
        cfg.setProperty("hibernate.format_sql", "true");
        cfg.setProperty("hibernate.current_session_context_class", "thread");

        // Connection pool (built-in)
        cfg.setProperty("hibernate.connection.pool_size", "10");

        // Register entities
        cfg.addAnnotatedClass(Brand.class);
        cfg.addAnnotatedClass(Cart.class);
        cfg.addAnnotatedClass(CartItem.class);
        cfg.addAnnotatedClass(Category.class);
        cfg.addAnnotatedClass(Order.class);
        cfg.addAnnotatedClass(OrderDetail.class);
        cfg.addAnnotatedClass(Product.class);
        cfg.addAnnotatedClass(ProductImage.class);
        cfg.addAnnotatedClass(ProductSpecification.class);
        cfg.addAnnotatedClass(Review.class);
        cfg.addAnnotatedClass(Role.class);
        cfg.addAnnotatedClass(User.class);
        cfg.addAnnotatedClass(Voucher.class);

        return cfg.buildSessionFactory();
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
