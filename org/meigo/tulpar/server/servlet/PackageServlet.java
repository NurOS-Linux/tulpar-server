package org.meigo.tulpar.server.servlet;

import org.meigo.tulpar.server.utils.PackageDownloadManager;
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

public class PackageServlet extends HttpServlet {
    private static final String PACKAGES_DIR = "package";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        String packageName = req.getParameter("package");
        String path = req.getParameter("path");

        if (packageName == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Package parameter is required.");
            return;
        }

        File packageDir = new File(PACKAGES_DIR, packageName);
        if (!packageDir.exists() || !packageDir.isDirectory()) {
            resp.sendRedirect("/errors/404.html?errorCode=404"); // Исправлено на 404
            return;
        }

        if (path == null) {
            // Если нет пути, скачиваем файл package.apg для этого пакета
            File packageFile = new File(packageDir, "download/" + packageName + ".apg"); // Исправлено на использование packageName
            if (!packageFile.exists()) {
                resp.sendRedirect("/errors/404.html?errorCode=404"); // Исправлено на 404
                return;
            }
            //downloadFile(packageFile, resp);
            PackageDownloadManager.downloadPackage(packageFile, req, resp);
        } else {
            // Если есть путь, показываем файл
            File fileToView = new File(packageDir, path);
            if (!fileToView.exists()) {
                resp.sendRedirect("/errors/404.html?errorCode=404"); // Исправлено на 404
                return;
            }
            showFile(fileToView, resp);
        }
    }

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






