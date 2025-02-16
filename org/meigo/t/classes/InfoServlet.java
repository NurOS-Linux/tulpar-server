package org.meigo.t.classes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InfoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String property = req.getParameter("property");

        if (property != null) {
            String value = System.getProperty(property);
            if (value != null) {
                resp.setContentType("text/plain; charset=UTF-8");
                resp.setHeader("Link", "<server-icon.png>; rel=\"icon\""); // Добавление иконки
                resp.getWriter().write(value); // Возвращаем только значение переменной
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Property not found.");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'property' is required.");
        }
    }
}



