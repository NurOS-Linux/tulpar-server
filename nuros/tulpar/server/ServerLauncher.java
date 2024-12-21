package nuros.tulpar.server;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.io.InputStream;

public class ServerLauncher extends Launcher {
    public ServerLauncher() {
    }
    
    public ServerLauncher(final String[] args) {
        super(args);
    }
    
    public ServerLauncher(final ClassLoader classLoader) {
        super(classLoader);
    }

    public void run(final boolean mustBootstrap, final boolean disableExtensions) throws Throwable {
        try {
            super.run(mustBootstrap, disableExtensions);
        }
        catch (final Throwable e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                final ExceptionDialog ld = new ExceptionDialog("Unexpected System Error!", e.getMessage(), e);
                ld.setDefaultCloseOperation(2);
                ld.setAlwaysOnTop(true);
                ld.expand();
                ld.setLocationRelativeTo((Component)null);
                ld.setVisible(true);
                return;
            });
        }
    }

    public static ServerLauncher current() {
        final Launcher current = Launcher.current();
        if (current instanceof ServerLauncher) {
            return (ServerLauncher)current;
        }
        return null;
    }

    public static void main(final String[] args) throws Throwable {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        final ServerLauncher launcher = new ServerLauncher(args);
        launcher.run();
    }
}
