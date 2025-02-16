package org.meigo.nuros.tulparserver.ssl;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class SSLManager {
    public static ServerConnector createSslConnector(Server server, int port, String keyStorePath, String keyStorePassword) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);

        ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
        sslConnector.setPort(port);
        return sslConnector;
    }

    public static void enableSsl(Server server, int port, String keyStorePath, String keyStorePassword) {
        ServerConnector sslConnector = createSslConnector(server, port, keyStorePath, keyStorePassword);
        server.addConnector(sslConnector);
    }
}
