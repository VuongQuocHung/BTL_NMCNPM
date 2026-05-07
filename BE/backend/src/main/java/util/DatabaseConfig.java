package util;

public class DatabaseConfig {

    // Đường dẫn kết nối đến MySQL
    public static final String URL = EnvConfig.get(
            "DB_URL",""
            
    );

    // Tên đăng nhập MySQL
    public static final String USERNAME = EnvConfig.get("DB_USERNAME", "");

    // Mật khẩu MySQL
    public static final String PASSWORD = EnvConfig.get("DB_PASSWORD", "");
}
