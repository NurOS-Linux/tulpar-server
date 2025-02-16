package org.meigo.t.classes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CustomErrorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String errorCode = req.getParameter("errorCode");
        if ("404".equals(errorCode)) {
            File errorPage = new File("errors/404.html");
            if (errorPage.exists()) {
                resp.setContentType("text/html; charset=UTF-8");
                resp.setHeader("Link", "<server-icon.png>; rel=\"icon\""); // Добавление иконки
                Files.copy(errorPage.toPath(), resp.getOutputStream());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Error page not found.");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown error code.");
        }
    }
}

