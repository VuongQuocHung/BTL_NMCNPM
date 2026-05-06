package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPath {
    private UploadPath() {
    }

    public static Path root() {
        String configured = System.getenv("UPLOAD_DIR");
        Path root = configured != null && !configured.isBlank()
                ? Paths.get(configured)
                : Paths.get(System.getProperty("user.dir"), "uploads");
        return root.toAbsolutePath().normalize();
    }
}
