package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/uploads/*")
public class ImageServlet extends HttpServlet {
    
    // Thư mục uploads nằm ở thư mục gốc của project (cùng cấp với src)
    private final Path rootLocation = Paths.get("c:/Users/phant/BTL_NMCNPM/BE/backend/uploads");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filename = req.getPathInfo();
        if (filename == null || filename.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name is missing");
            return;
        }

        // Bỏ dấu gạch chéo đầu tiên
        filename = filename.substring(1);
        File file = rootLocation.resolve(filename).toFile();

        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            return;
        }

        // Tự động detect Content-Type dựa trên đuôi file
        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        resp.setContentType(mimeType);
        resp.setContentLength((int) file.length());

        // Đọc file và ghi ra response
        try (FileInputStream in = new FileInputStream(file); 
             OutputStream out = resp.getOutputStream()) {
             
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
