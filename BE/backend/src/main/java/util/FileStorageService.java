package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

public class FileStorageService {
    private final Path rootLocation = Paths.get("c:/Users/phant/BTL_NMCNPM/BE/backend/uploads");

    public String storeFile(InputStream inputStream, String originalFilename) {
        try {
            if (!Files.exists(rootLocation)) Files.createDirectories(rootLocation);
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path dest = rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (!dest.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            Files.copy(inputStream, dest, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}
