package util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.UUID;

public class FileStorageService {
    private final Path rootLocation = uploadRootLocation();

    public static Path uploadRootLocation() {
        String configuredDir = EnvConfig.get("UPLOAD_DIR", "");
        if (configuredDir != null && !configuredDir.isBlank()) {
            return Paths.get(configuredDir).toAbsolutePath().normalize();
        }

        Path webappRoot = deployedWebappRoot();
        if (webappRoot != null) {
            return webappRoot.resolve("uploads").toAbsolutePath().normalize();
        }

        return Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
    }

    private static Path deployedWebappRoot() {
        try {
            Path classesDir = Paths.get(FileStorageService.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).toAbsolutePath().normalize();
            Path current = Files.isRegularFile(classesDir) ? classesDir.getParent() : classesDir;
            while (current != null) {
                if ("WEB-INF".equalsIgnoreCase(current.getFileName().toString())) {
                    return current.getParent();
                }
                current = current.getParent();
            }
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            return null;
        }
        return null;
    }

    public String storeFile(InputStream inputStream, String originalFilename) {
        try {
            if (!Files.exists(rootLocation)) Files.createDirectories(rootLocation);
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path dest = rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (!dest.getParent().equals(rootLocation)) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            Files.copy(inputStream, dest, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in " + rootLocation + ": " + e.getMessage(), e);
        }
    }
}
