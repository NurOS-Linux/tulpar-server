package org.meigo.t.classes;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.meigo.t.TExtension;
import php.runtime.annotation.Reflection;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.lang.BaseObject;
import php.runtime.reflection.ClassEntity;

@Reflection.Namespace(TExtension.NS)
public class TulparServer extends BaseObject {
    public TulparServer(Environment env) {
        super(env);
    }

    protected TulparServer(ClassEntity entity) {
        super(entity);
    }

    public TulparServer(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public static int start(int Port) throws Exception {
        // Создаём сервер на порту 8080
        Server server = new Server(Port);

        // Создаём контекст сервера
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Маршруты
        context.addServlet(new ServletHolder(new InfoServlet()), "/api/v2.0/info");
        context.addServlet(new ServletHolder(new PackageServlet()), "/api/v2.0/get"); // Работа с параметрами package и path
        context.addServlet(new ServletHolder(new PackageDownloadServlet()), "/package/*"); // Скачивание пакетов
        context.addServlet(new ServletHolder(new StaticPageServlet()), "/"); // Главная страница (статическая)
        context.addServlet(new ServletHolder(new CustomErrorServlet()), "/errors/404.html"); // Страница ошибки 404
        context.addServlet(new ServletHolder(new StaticFileServlet()), "/icons/*"); // Иконка сайта
        context.addServlet(new ServletHolder(new PackageListServlet()), "/api/v2.0/list"); // Список пакетов в JSON

        // Устанавливаем обработчик для статических файлов (например, для страницы ошибки)
        context.addServlet(new ServletHolder(new StaticFileServlet()), "/static/*");

        // Устанавливаем контекст на сервер
        server.setHandler(context);

        // Запуск сервера
        server.start();
        server.join();

        return 123;
    }
}
