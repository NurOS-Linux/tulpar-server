package meigo;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Console {

    protected static boolean __RN = false;

    public static boolean isXTerm() {
        String term = System.getenv("TERM");
        return term != null && term.equalsIgnoreCase("xterm");
    }

    public static void logTask(String message, Object... args) {
        print("  " + message, 10, args);
    }

    public static void logValue(String key, String value) {
        int width = getTerminalWidth();
        int keyWidth = clearText(key).length();
        int valueWidth = clearText(value).length();

        String message = "  " + key + " " + Colors.withColor(".".repeat(width - keyWidth - valueWidth - 6), "gray") + " " + value + "  ";
        print(message);
        System.out.println();
        __RN = false;
    }

    public static void logTaskResult(boolean fail) {
        print(".. " + (fail ? Colors.withColor("FAIL", "red") : Colors.withColor("DONE", "green")));
        System.out.println();
        __RN = false;
    }

    protected static String clearText(String message) {
        return Pattern.compile("\\e\\[[0-9;]*m").matcher(message).replaceAll("");
    }

    public static void log(String message, String r, Object... args) {
        print(message, 0, args);
        System.out.print(r);
        __RN = false;
    }

    public static void printForXterm(String message, Object... args) {
        if (isXTerm()) {
            print(message, 0, args);
        }
    }

    public static void print(String message, int offset, Object... args) {
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", Colors.withColor(args[i].toString(), "green"));
        }

        if (offset > 0) {
            int width = getTerminalWidth() - offset;
            int messageWidth = clearText(message).length();

            if (messageWidth > width) {
                message = message.substring(0, width - 1) + ' ';
            } else if (messageWidth < width) {
                message = message + ' ' + ".".repeat(width - messageWidth - 1);
            }
        }

        System.out.print(message);
    }

    public static void debug(String message, Object... args) {
        if (isDebug()) {
            log(Colors.withColor("(debug)", "silver") + " " + message, "\n", args);
        }
    }

    public static void warn(String message, String r, Object... args) {
        log((__RN ? "  " : r + "  ") + Colors.withColor(" WARN ", "yellow_bg") + " " + message + " ", r, args);
        __RN = true;
    }

    public static void error(String message, Object... args) {
        log((__RN ? "  " : "\n  ") + Colors.withColor(" FAIL ", "red_bg") + " " + message + " ", "\n", args);
        __RN = true;
    }

    public static void info(String message, Object... args) {
        log((__RN ? "  " : "\n  ") + Colors.withColor(" INFO ", "magenta_bg") + " " + message + " ", "\n", args);
        __RN = true;
    }

    public static void badged(String message, String badge, String color, Object... args) {
        log((__RN ? "  " : "\n  ") + Colors.withColor(badge, color) + message + "\n", "\n", args);
        __RN = true;
    }

    public static void returnValue(String key, String value) {
        print("\r" + (char) 27 + "[F");
        logValue(key, value);
    }

    public static boolean readYesNo(String message, boolean defaultVal) {
        String result = read(message + " (Y/n)", defaultVal ? "yes" : "no").toLowerCase();

        if (result.equals("yes") || result.equals("y")) return true;
        if (result.equals("no") || result.equals("n")) return false;

        log(" -> please enter " + Colors.withColor("Y", "green") + " (yes) or " + Colors.withColor("N", "yellow") + " (no), try again ...");

        return readYesNo(message, defaultVal);
    }

    public static String read(String message, String defaultVal) {
        System.out.print(message + " ");
        if (defaultVal != null) {
            System.out.print("(default: " + Colors.withColor(defaultVal, "green") + ") ");
        }

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (line.isEmpty()) {
            returnValue(message, defaultVal);
            return defaultVal;
        }

        returnValue(message, line);
        return line;
    }
}
