package org.meigo.tulpar.server.servlet;

import org.meigo.tulpar.server.Logger;
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
import java.io.PrintWriter;
import java.nio.file.Files;

public class VadimAPIServlet extends HttpServlet {
    // Корневая папка с пакетами (относительно рабочей директории приложения)
    private static final String PACKAGES_ROOT = "packages";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        Logger.info("Received request: " + req.getRequestURI());
        String pathInfo = req.getPathInfo();
        Logger.info("Path info: " + pathInfo);
        if (pathInfo == null || pathInfo.isEmpty()) {
            Logger.error("Empty path info.");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format.");
            return;
        }
        // Ожидается URL вида /{packageName}/{command} (например, /nano/download или /nano/info)
        String[] parts = pathInfo.split("/");
        if (parts.length < 3) {
            Logger.error("Insufficient path segments: " + pathInfo);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format. Expected /{packageName}/{command}");
            return;
        }
        String packageName = parts[1];
        String command = parts[2];
        Logger.info("Parsed package name: " + packageName);
        Logger.info("Parsed command: " + command);

        if ("download".equalsIgnoreCase(command)) {
            processDownload(req, resp, packageName);
        } else if ("info".equalsIgnoreCase(command)) {
            processInfo(req, resp, packageName);
        } else {
            Logger.error("Unknown command: " + command);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown command: " + command);
        }
    }

    private void processDownload(HttpServletRequest req, HttpServletResponse resp, String packageName) throws IOException {
        Logger.info("Processing download for package: " + packageName);
        String arch = req.getParameter("arch");
        String version = req.getParameter("version");
        Logger.info("Received parameters: arch = " + arch + ", version = " + version);
        if (arch == null || version == null) {
            Logger.error("Missing 'arch' or 'version' parameters.");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameters 'arch' and 'version' are required.");
            return;
        }
        // Формируем путь к файлу: packages/{packageName}/{arch}/{version}.apg
        File file = new File(PACKAGES_ROOT + File.separator + packageName + File.separator + arch, version + ".apg");
        Logger.info("Looking for file: " + file.getAbsolutePath());
        if (!file.exists() || !file.isFile()) {
            Logger.error("File not found: " + file.getAbsolutePath());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested file not found.");
            return;
        }
        Logger.info("File found: " + file.getAbsolutePath());
        // Формируем новое имя для скачиваемого файла: {packageName}-{version}_{arch}.apg
        String downloadFileName = packageName + "-" + version + "_" + arch + ".apg";
        Logger.info("Download file will be named: " + downloadFileName);
        PackageDownloadManager.downloadPackage(file, req, resp);
        Logger.info("File download completed successfully.");
    }

    private void processInfo(HttpServletRequest req, HttpServletResponse resp, String packageName) throws IOException {
        Logger.info("Processing info request for package: " + packageName);
        // Путь к файлу metadata.json: packages/{packageName}/metadata.json
        File metaFile = new File(PACKAGES_ROOT + File.separator + packageName, "metadata.json");
        Logger.info("Looking for metadata file: " + metaFile.getAbsolutePath());
        if (!metaFile.exists() || !metaFile.isFile()) {
            Logger.error("Metadata file not found: " + metaFile.getAbsolutePath());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Metadata file not found.");
            return;
        }
        String content = new String(Files.readAllBytes(metaFile.toPath()), "UTF-8");
        Logger.info("Metadata file read successfully. Content length: " + content.length());
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(content);
        }
        Logger.info("Metadata response sent successfully.");
    }
}
