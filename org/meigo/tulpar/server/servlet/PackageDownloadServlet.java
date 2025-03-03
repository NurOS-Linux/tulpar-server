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

public class PackageDownloadServlet extends HttpServlet {
    // Папка с пакетами, располагается рядом с программой
    private static final String PACKAGES_DIR = "package";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Ограничение по количеству запросов и логирование IP + путь
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        // Ожидается URL вида: /package/<название>.apg
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            resp.sendRedirect("/errors/404.html?errorCode=404");
            return;
        }

        // Убираем ведущий слеш
        String fileName = pathInfo.substring(1);

        // Проверяем, что имя файла заканчивается на ".apg"
        if (!fileName.endsWith(".apg")) {
            resp.sendRedirect("/errors/404.html?errorCode=404");
            return;
        }

        // Формируем полный путь к файлу в папке package
        File packageFile = new File(PACKAGES_DIR, fileName);
        if (!packageFile.exists() || packageFile.isDirectory()) {
            resp.sendRedirect("/errors/404.html?errorCode=404");
            return;
        }

        // Отдаем файл клиенту для скачивания
        downloadFile(packageFile, resp);
    }

    // Метод для скачивания файла
    private void downloadFile(File file, HttpServletResponse resp) throws IOException {
        String fileName = file.getName();
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentType("application/octet-stream");

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
