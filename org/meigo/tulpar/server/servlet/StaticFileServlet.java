package org.meigo.tulpar.server.servlet;

import org.meigo.tulpar.server.utils.RequestLimiter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileServlet extends HttpServlet {
    private static final String ICONS_DIR = "icons"; // Папка для иконки

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        String path = req.getRequestURI().substring("/icons/".length()); // Убираем часть /icons/
        File file = new File(ICONS_DIR, path);

        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Icon not found.");
            return;
        }

        // Определяем MIME-тип файла
        String contentType = Files.probeContentType(file.toPath());
        resp.setContentType(contentType != null ? contentType : "application/octet-stream");

        // Отправляем файл
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}

