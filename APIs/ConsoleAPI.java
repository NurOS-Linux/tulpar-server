package APIs;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class ConsoleAPI {
    private static Boolean isTerm = null;
    private static Boolean isColorSupport = null;

    public static boolean isXTerm() {
        if (isTerm != null) {
            return isTerm;
        }

        String term = System.getenv("TERM");
        return isTerm = ("xterm".equalsIgnoreCase(term));
    }
    public static boolean hasColorSupport() {
        if (isColorSupport != null) {
            return isColorSupport;
        }

        if (isXTerm()) {
            return isColorSupport = true;
        }

      return isColorSupport = false;

    }

    public static int getWidth() {
        AnsiConsole.systemInstall();
        int width = AnsiConsole.getTerminalWidth();
        AnsiConsole.systemUninstall();
        return width;
    }
}
