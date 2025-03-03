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

public class PackageFileServlet extends HttpServlet {
    private static final String PACKAGES_DIR = "package";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        String pathInfo = req.getPathInfo(); // /example/manifest.json
        if (pathInfo == null || pathInfo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File path is required.");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path.");
            return;
        }

        String packageName = pathParts[1];
        String filePath = pathInfo.substring(("/" + packageName).length());

        File packageDir = new File(PACKAGES_DIR, packageName);
        if (!packageDir.exists() || !packageDir.isDirectory()) {
            resp.sendRedirect("/errors/404.html?errorCode=Package not found.");
            return;
        }

        File fileToView = new File(packageDir, filePath);
        if (!fileToView.exists()) {
            resp.sendRedirect("/errors/404.html?errorCode=File not found.");
            return;
        }

        showFile(fileToView, resp); // Открываем файл для просмотра
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
}
