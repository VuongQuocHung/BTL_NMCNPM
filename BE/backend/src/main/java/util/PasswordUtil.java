package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password encoder utility (thay thế Spring PasswordEncoder).
 */
public class PasswordUtil {

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
