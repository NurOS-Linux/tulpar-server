package server;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ServerWrapper.class);
    private Server server;

    public ServerWrapper(int port) {
        server = new Server(port);
        logger.info("Server initialized on port {}", port);
    }

    public void startServer() {
        try {
            server.start();
            logger.info("Server started successfully on port {}", server.getURI().getPort());
            server.join();
        } catch (Exception e) {
            logger.error("Failed to start the server", e);
        }
    }

    public void stopServer() {
        try {
            server.stop();
            logger.info("Server stopped successfully.");
        } catch (Exception e) {
            logger.error("Failed to stop the server", e);
        }
    }

    public Server getServer() {
        return server;
    }
}
