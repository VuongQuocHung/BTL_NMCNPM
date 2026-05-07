package util;

public class DatabaseConfig {

    // Đường dẫn kết nối đến MySQL
    public static final String URL = EnvConfig.get(
            "DB_URL",
            "jdbc:mysql://localhost:3306/laptop_store?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8"
    );

    // Tên đăng nhập MySQL
    public static final String USERNAME = EnvConfig.get("DB_USERNAME", "root");

    // Mật khẩu MySQL
    public static final String PASSWORD = EnvConfig.get("DB_PASSWORD", "");
}
