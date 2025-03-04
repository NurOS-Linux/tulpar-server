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

public class IndexServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Проверка запроса через RequestLimiter
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        // Файл index.html должен располагаться в папке static
        File file = new File("static/index.html");
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Index page not found.");
            return;
        }

        resp.setContentType("text/html; charset=UTF-8");
        resp.setContentLength((int) file.length());
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}
