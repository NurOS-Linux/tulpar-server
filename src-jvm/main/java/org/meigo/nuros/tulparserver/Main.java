package org.meigo.nuros.tulparserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        // Создаем сервер на порту 8080
        Server server = new Server(8080);

        // Создаем обработчик сервлетов
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        // Добавляем сервлет
        handler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setContentType("text/html");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println("<h1>Hello from Tulpar-Server!</h1>");
            }
        }), "/hello");

        // Запускаем сервер
        server.start();
        server.join();
    }
}
