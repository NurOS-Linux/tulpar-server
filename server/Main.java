package server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Main {
    public static void main(String[] args) {
        ServerWrapper serverWrapper = new ServerWrapper(8080);
        ServletContextHandlerWrapper contextHandlerWrapper = new ServletContextHandlerWrapper();
        contextHandlerWrapper.addServlet(MyServlet.class, "/hello");
        RequestHandler requestHandler = new RequestHandler();
        serverWrapper.getServer().setHandler(requestHandler);
        serverWrapper.getServer().setHandler(contextHandlerWrapper.getContextHandler());
        serverWrapper.startServer();
    }
}
