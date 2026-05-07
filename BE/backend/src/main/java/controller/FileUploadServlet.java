package controller;

import util.ServiceLocator;
import util.FileStorageService;
import util.JsonUtil;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Map;

@MultipartConfig(maxFileSize = 10485760, maxRequestSize = 10485760)
public class FileUploadServlet extends BaseServlet {
    private FileStorageService fileStorageService;
    @Override public void init() { fileStorageService = ServiceLocator.getInstance().fileStorageService; }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Part filePart = req.getPart("file");
            if (filePart == null) { JsonUtil.writeError(resp, 400, "No file uploaded"); return; }
            String fileName = fileStorageService.storeFile(filePart.getInputStream(), filePart.getSubmittedFileName());
            String imagePath = "/uploads/" + fileName;
            String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
            JsonUtil.writeJson(resp, 200, Map.of(
                    "fileName", fileName,
                    "url", imagePath,
                    "imageUrl", imagePath,
                    "absoluteUrl", baseUrl + imagePath
            ));
        } catch (Exception e) {
            JsonUtil.writeError(resp, 500, "Upload failed: " + e.getMessage());
        }
    }
}
