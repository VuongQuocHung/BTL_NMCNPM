package controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.FileStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImageServlet extends HttpServlet {
    private final List<Path> rootLocations = FileStorageService.readableUploadRootLocations();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filename = req.getPathInfo();
        if (filename == null || filename.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name is missing");
            return;
        }

        filename = filename.substring(1);
        Path filePath = findImage(filename, resp);
        if (filePath == null) {
            return;
        }

        String mimeType = getServletContext().getMimeType(filePath.getFileName().toString());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        resp.setContentType(mimeType);
        resp.setContentLengthLong(Files.size(filePath));

        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private Path findImage(String filename, HttpServletResponse resp) throws IOException {
        for (Path rootLocation : rootLocations) {
            Path filePath = rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (!filePath.startsWith(rootLocation)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file path");
                return null;
            }
            if (Files.isRegularFile(filePath)) {
                return filePath;
            }
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
        return null;
    }
}
