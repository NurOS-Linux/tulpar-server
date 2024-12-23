package server;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextHandlerWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ServletContextHandlerWrapper.class);
    private ServletContextHandler contextHandler;

    public ServletContextHandlerWrapper() {
        contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        logger.info("ServletContextHandler initialized");
    }

    public void addServlet(Class servletClass, String path) {
        ServletHolder servletHolder = new ServletHolder(servletClass);
        contextHandler.addServlet(servletHolder, path);
        logger.info("Servlet added to path: {}", path);
    }

    public ServletContextHandler getContextHandler() {
        return contextHandler;
    }
}
