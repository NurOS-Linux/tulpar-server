import com.google.gson.Gson;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;

public class ServerLauncher {

    private Config config;

    public ServerLauncher() {
        loadConfig();
        String hello1 = config.getCli().getHello()[0];
        String hello2 = config.getCli().getHello()[1];
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        System.out.println(Ansi.ansi().bgCyan().a(hello1).reset() + " " + osName);
        System.out.println(Ansi.ansi().bgCyan().a(hello2).reset() + " " + osArch);
        System.out.println("Reached target " + Ansi.ansi().fg(Ansi.Color.GREEN).a("Terminal Width").reset());
        System.out.println("Terminal width: " + this.width);
        String address = config.getServer().getAddress();
        int port = config.getServer().getPort();
        boolean runInBackground = config.getServer().isRunInBackground();
        System.out.println("Server Address: " + address);
        System.out.println("Server Port: " + port);
        System.out.println("Run in Background: " + runInBackground);
        startServer(address, port, runInBackground);
    }

    private void loadConfig() {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader("res://config.json");
            this.config = gson.fromJson(reader, Config.class);
            reader.close();
            System.out.println("Configuration loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading configuration file.");
            e.printStackTrace();
        }
    }

    private void startServer(String address, int port, boolean runInBackground) {
        try {
            Server server = new Server(port);
            ServletHandler handler = new ServletHandler();
            server.setHandler(handler);
            handler.addServletWithMapping(HelloServlet.class, "/");
            if (runInBackground) {
                System.out.println("Server running in background...");
                new Thread(() -> {
                    try {
                        server.start();
                        server.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                System.out.println("Server running in foreground...");
                server.start();
                server.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerLauncher();
    }
}
