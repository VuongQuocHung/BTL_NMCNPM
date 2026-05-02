package util;

import dao.RoleDao;
import dao.UserDao;
import model.Role;
import model.User;
import util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.Locale;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== VPH Laptop Store: Initializing ===");
        // 1. Khởi tạo Hibernate
        HibernateUtil.getSessionFactory();
        System.out.println("Hibernate SessionFactory initialized.");

        // 2. Khởi tạo ServiceLocator (eager init all services)
        ServiceLocator.getInstance();
        System.out.println("ServiceLocator initialized.");

        // 3. Khởi tạo roles và admin mặc định
        initData();

        // 4. Khởi tạo sản phẩm mẫu
        DataInitializer.initSampleProducts();

        System.out.println("=== VPH Laptop Store: Ready ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== VPH Laptop Store: Shutting down ===");
        HibernateUtil.shutdown();
    }

    private void initData() {
        RoleDao roleDao = ServiceLocator.getInstance().roleDao;
        UserDao userDao = ServiceLocator.getInstance().userDao;

        // Tạo roles
        Role adminRole = roleDao.findByName("ADMIN")
                .orElseGet(() -> roleDao.save(Role.builder().name("ADMIN").build()));
        roleDao.findByName("CUSTOMER")
                .orElseGet(() -> roleDao.save(Role.builder().name("CUSTOMER").build()));

        // Tạo admin user mặc định
        String adminEmail = EnvConfig.get("ADMIN_EMAIL", "admin@gmail.com").trim().toLowerCase(Locale.ROOT);
        String adminPassword = EnvConfig.get("ADMIN_PASSWORD", "admin123");
        String adminFullName = EnvConfig.get("ADMIN_FULLNAME", "System Admin");

        if (!userDao.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(PasswordUtil.encode(adminPassword))
                    .fullName(adminFullName)
                    .phone("0000000000")
                    .role(adminRole)
                    .enabled(true)
                    .build();
            userDao.save(admin);
            System.out.println("Default admin created: " + adminEmail);
        }
    }
}
