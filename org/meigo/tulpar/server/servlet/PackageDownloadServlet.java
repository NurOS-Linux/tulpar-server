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

public class PackageDownloadServlet extends HttpServlet {
    private static final String PACKAGES_DIR = "package";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        String pathInfo = req.getPathInfo(); // Путь, например "/example/manifest.json"
        if (pathInfo == null || pathInfo.isEmpty()) {
            resp.sendRedirect("/errors/404.html?errorCode=404"); // Если не указан путь, 404
            return;
        }

        String packageName = pathInfo.split("/")[1]; // Извлекаем имя пакета, например "example"
        File packageDir = new File(PACKAGES_DIR, packageName);
        if (!packageDir.exists() || !packageDir.isDirectory()) {
            resp.sendRedirect("/errors/404.html?errorCode=404"); // Пакет не найден
            return;
        }

        // Если путь указывает на файл, отображаем содержимое
        if (pathInfo.contains(".")) {
            String filePath = pathInfo.substring(pathInfo.indexOf(packageName) + packageName.length() + 1); // путь к файлу
            File fileToView = new File(packageDir, filePath);
            if (!fileToView.exists()) {
                resp.sendRedirect("/errors/404.html?errorCode=404"); // Файл не найден
                return;
            }
            showFile(fileToView, resp); // Отображаем файл
        } else {
            // Скачивание архива пакета, если не указан путь к файлу
            File packageFile = new File(packageDir, "download/" + packageName + ".apg");
            if (!packageFile.exists()) {
                resp.sendRedirect("/errors/404.html?errorCode=404"); // Файл архива не найден
                return;
            }
            downloadFile(packageFile, resp); // Скачиваем файл
        }
    }

    // Метод для отображения файла
    private void showFile(File file, HttpServletResponse resp) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        resp.setContentType(contentType != null ? contentType : "application/octet-stream");

        try (FileInputStream fis = new FileInputStream(file); OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    // Метод для скачивания файла
    private void downloadFile(File file, HttpServletResponse resp) throws IOException {

        String fileName = file.getName();
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentType("application/octet-stream");

        try (FileInputStream fis = new FileInputStream(file); OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}
