package util;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Đọc biến môi trường từ file .env hoặc system environment.
 */
public class EnvConfig {
    private static final Dotenv dotenv;

    static {
        Dotenv loaded;
        try {
            loaded = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            loaded = null;
        }
        dotenv = loaded;
    }

    public static String get(String key) {
        // Ưu tiên system env, fallback .env file
        String val = System.getenv(key);
        if (val != null && !val.isBlank()) return val;
        if (dotenv != null) {
            val = dotenv.get(key);
        }
        return val;
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public static long getLong(String key, long defaultValue) {
        String val = get(key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
