package org.meigo.tulpar.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;

import org.eclipse.jetty.servlet.ServletHolder;
import org.meigo.tulpar.server.servlet.*;
import org.meigo.tulpar.server.utils.CLI;
import org.meigo.tulpar.server.utils.InMemoryRequestLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TServer {

    public static Server tserver;

    private static InMemoryRequestLog requestLog;

    public static boolean start() throws Exception {
        Logger.devinfo("Configuring the server...");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(Config.serveraddress, Config.serverport);
        tserver = new Server(inetSocketAddress);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Маршруты
        context.addServlet(new ServletHolder(new InfoServlet()), "/api/v2.0/info");
        context.addServlet(new ServletHolder(new PackageServlet()), "/api/v2.0/get"); // Работа с параметрами package и path
        context.addServlet(new ServletHolder(new PackageDownloadServlet()), "/package/*"); // Скачивание пакетов
        Logger.warn("The PackageDownloadServlet (or /package/*) route has been changed to work only for downloading packages.");
        //context.addServlet(new ServletHolder(new StaticPageServlet()), "/"); // Главная страница (статическая)
        Logger.warn("StaticPage deprecated");
        context.addServlet(new ServletHolder(new CustomErrorServlet()), "/errors/404.html"); // Страница ошибки 404
        context.addServlet(new ServletHolder(new StaticFileServlet()), "/icons/*"); // Иконка сайта
        context.addServlet(new ServletHolder(new PackageListServlet()), "/api/v2.0/list"); // Список пакетов в JSON

        context.addServlet(new ServletHolder(new FaviconServlet()), "/favicon.ico");
        context.addServlet(new ServletHolder(new IndexServlet()), "/index.html");
        Logger.success("Routes added");


        // Устанавливаем обработчик для статических файлов (например, для страницы ошибки)
        context.addServlet(new ServletHolder(new StaticFileServlet()), "/static/*");

        // Устанавливаем контекст на сервер
        tserver.setHandler(context);

        requestLog = new InMemoryRequestLog();
        tserver.setRequestLog(requestLog);

        new Thread(() -> {
            try {
                tserver.start();
                tserver.join();
            } catch (Exception e) {
                Logger.error("Error while running server: " + e.getMessage());
                System.exit(-1);
            }
        }).start();
        Logger.success("Server started at " + Config.serveraddress + ":" + Config.serverport);
        long endTime = System.currentTimeMillis(); // Время после старта
        double loadTime = (endTime - Main.startTime) / 1000.0; // Разница в секундах
        Logger.success("Server started in " + loadTime + " seconds.");

        pingServer(Config.serveraddress, Config.serverport);


        CLI.init();
        return true;
    }

    public static void pingServer(String ip, int port) {
        try {
            long startTime = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), 5000); // Тайм-аут 5 секунд
            }
            long endTime = System.currentTimeMillis();
            long ping = endTime - startTime;
            Logger.success("Ping to " + ip + ":" + port + " = " + ping + " ms");
        } catch (IOException e) {
            Logger.error("Failed to connect to " + ip + ":" + port + " (timeout or unreachable)");
        }
    }

    // Метод для получения логов запросов
    public static InMemoryRequestLog getRequestLog() {
        return requestLog;
    }

}
