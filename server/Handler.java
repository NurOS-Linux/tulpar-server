package server;

import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("Handling request for: {}", target);
            response.setContentType("text/html; charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Request handled by Jetty</h1>");
            baseRequest.setHandled(true);
        } catch (Exception e) {
            logger.error("Error while handling request", e);
        }
    }
}
