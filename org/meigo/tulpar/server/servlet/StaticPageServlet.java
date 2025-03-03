package org.meigo.tulpar.server.servlet;

import org.meigo.tulpar.server.Logger;
import org.meigo.tulpar.server.utils.RequestLimiter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StaticPageServlet extends HttpServlet {
    private static final String STATIC_DIR = "static"; // Папка с вашими статичными файлами

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        File indexFile = new File(STATIC_DIR, "index.html");
        Logger.devinfo("Кто-то зашел на index.html " + req.getRemoteAddr());
        if (indexFile.exists()) {
            resp.setContentType("text/html; charset=UTF-8");
            Files.copy(indexFile.toPath(), resp.getOutputStream());
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Static page not found.");
        }
    }
}
