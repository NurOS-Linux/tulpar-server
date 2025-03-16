package org.meigo.tulpar.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import org.eclipse.jetty.server.Server;
import org.fusesource.jansi.AnsiConsole;
import org.meigo.tulpar.server.Config;
import org.meigo.tulpar.server.utils.DownloadManager;
import org.meigo.tulpar.server.utils.PackageScanner;
import org.meigo.tulpar.server.utils.UpdateManager;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

import static org.meigo.tulpar.server.Config.config;
import static org.meigo.tulpar.server.api.python.pyInterp;
import static org.meigo.tulpar.server.sysinfo.*;


/**
 * =====================================================================
 * Tulpar Server Main Class @ DEV BY meigo inc. \ discord: @glebbb tg: @numarktop1gg
 * =====================================================================
 * This class is the entry point for the Tulpar Server application.
 *
 * Key responsibilities include:
 * - Initializing system information and configuration.
 * - Loading and setting up a Python interpreter for plugin support.
 * - Verifying system architecture compatibility.
 * - Reading server configuration parameters from a JSON file.
 * - Displaying a welcome message with configurable ANSI color settings.
 * - Enabling terminal enhancements on Windows (e.g., ANSI escape codes, UTF-8).
 * - Starting the server.
 *
 * Important details for developers:
 * - The configuration is loaded from "config.json" via the Config class.
 * - ANSI colors for terminal output are determined based on the "cli" configuration.
 * - The version of the application is set using a static variable (VERSION) for
 *   easier maintenance and potential future upgrades.
 * - Utility methods are provided for tasks such as string repetition and architecture checks.
 *
 * Each method is documented with its purpose to aid first-time developers in
 * understanding the code structure and functionality.
 *
 * =====================================================================
 */
public class Main {

    // Version information for the server. Update this value to reflect new releases.
    public static final String VERSION = "v2.0-beta.3";

    // Record the application start time in milliseconds.
    public static long startTime = System.currentTimeMillis();

    /**
     * Main method: entry point of the application.
     *
     * It performs the following tasks:
     * - Initializes system info and configuration.
     * - Loads the Python interpreter for plugin support.
     * - Validates the system architecture.
     * - Reads and applies server configuration settings.
     * - Displays a welcome message with ANSI color formatting.
     * - Starts the server.
     *
     * @param args Command-line arguments.
     * @throws Exception If initialization or server startup fails.
     */
    public static void main(String[] args) throws Exception {
        checkServerAvailability();



        // Initialize system information and log the initialization status.
        sysinfo.init();
        Logger.devinfo("Init ...");
        //UpdateManager.checkForUpdates();

        // Load configuration from file and set it up.
        Config.set("config.json");
        Config.load();

        // Load Python 2.7 interpreter for plugin support.
        Logger.info("Loading python 2.7 for plugins ...");
        pyInterp.exec("print('[org.meigo.tulpar.server.api.python] Loaded!')");



        // Check system architecture compatibility.
        if (isSupportedArchitecture(arch)) {
            Logger.devinfo("The software is supported on this architecture: " + arch);
        }
        // The following architecture check is commented out for now.
        // else {
        //    Logger.error("The software is not supported on this architecture: " + arch);
        //    System.exit(1);
        //}

        // Log server initialization progress.
        Logger.info("Server Initialisation ...");

        // Load server configuration values from the config.
        Config.apiconfig = Integer.parseInt(Objects.requireNonNull(Config.get("api.config")));
        Config.serveraddress = Config.get("server.address");
        Config.serverport = Integer.parseInt(Objects.requireNonNull(Config.get("server.port")));
        Config.serverrunInBackground = Config.get("server.runInBackground");
        Config.servermaxRequests = Integer.parseInt(Objects.requireNonNull(Config.get("server.maxRequests")));
        Config.serverlogFile = Config.get("server.logFile");
        Config.serverhttpsRedirect = Config.get("server.httpsRedirect").isEmpty();

        Config.blockDurationMillis = Integer.parseInt(Objects.requireNonNull(Config.get("server.blockDurationMillis")));

        Config.MAX_DOWNLOADS_PER_IP = Integer.parseInt(Objects.requireNonNull(Config.get("server.maxDownloadsPerIP")));
        Config.MAX_DOWNLOAD_SPEED = Integer.parseInt(Objects.requireNonNull(Config.get("server.maxDownloadSpeed")));
        Config.BUFFER_SIZE = Integer.parseInt(Objects.requireNonNull(Config.get("server.BUFFER_SIZE")));

        System.out.println("loading...");
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            PackageScanner.scanAndUnpack(pyInterp);
        }

        // Read CLI configuration for color and welcome message.
        JsonObject cli = config.getAsJsonObject("cli");
        String colorName = cli.get("color").getAsString();
        String ansiColor = getAnsiColor(colorName);

        // Retrieve the welcome message lines from the configuration.
        JsonArray helloArray = cli.getAsJsonArray("hello");
        String helloLine1 = helloArray.get(0).getAsString();
        String helloLine2 = helloArray.get(1).getAsString();

        // Print the welcome messages with the configured ANSI color.
        System.out.println(ansiColor + helloLine1 + " " + arch + " | " + os + " | " + osver);
        // Use the VERSION variable instead of a hardcoded version string.
        System.out.println(ansiColor + helloLine2 + " " + VERSION + " " + "\u001B[0m");

        // Determine terminal width for printing a divider line.
        int width = AnsiConsole.getTerminalWidth();
        int oldwidth = width;
        if (width <= 0) { // if terminal width is not defined, use a default value
            width = 60;
        }
        System.out.println(ansiColor + repeat("=", width) + "\u001B[0m");

        // Warn users that this is a beta version and may contain security issues.
        Logger.warn("This is a test beta version of TulparServer. The product is not ready for full operation, there may be security issues.");

        // Log an error if the terminal width could not be determined.
        if (oldwidth <= 0) {
            Logger.error("We were unable to determine the width of the terminal, is it definitely a terminal?");
        }

        // Start the server.
        TServer.start();
    }

    /**
     * Checks if the provided architecture is supported.
     *
     * Supported architectures include various forms of x86 and x86_64.
     *
     * @param arch The architecture string to check.
     * @return true if supported; false otherwise.
     */
    private static boolean isSupportedArchitecture(String arch) {
        return arch.equals("amd64") || arch.equals("x86_64") ||
                arch.equals("i386") || arch.equals("i486") ||
                arch.equals("i586") || arch.equals("i686") ||
                arch.equals("x86");
    }

    /**
     * Repeats a given string a specified number of times.
     *
     * This utility method is used to create a visual divider in the terminal.
     *
     * @param str   The string to be repeated.
     * @param count The number of repetitions.
     * @return A new string consisting of the original string repeated.
     */
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Returns the ANSI escape code corresponding to a given color name.
     *
     * The method supports standard colors, bright colors, and background colors.
     * If the provided color name does not match any known code, an empty string is returned.
     *
     * @param colorName The name of the color as specified in the configuration.
     * @return The ANSI escape code for the specified color.
     */
    private static String getAnsiColor(String colorName) {
        String ansiColor;
        switch (colorName.toLowerCase()) {
            // Standard colors
            case "black":
                ansiColor = "\u001B[30m";
                break;
            case "red":
                ansiColor = "\u001B[31m";
                break;
            case "green":
                ansiColor = "\u001B[32m";
                break;
            case "yellow":
                ansiColor = "\u001B[33m";
                break;
            case "blue":
                ansiColor = "\u001B[34m";
                break;
            case "magenta":
                ansiColor = "\u001B[35m";
                break;
            case "cyan":
                ansiColor = "\u001B[36m";
                break;
            case "white":
                ansiColor = "\u001B[37m";
                break;

            // Bright colors
            case "black_bright":
            case "bright_black":
                ansiColor = "\u001B[90m";
                break;
            case "red_bright":
            case "bright_red":
                ansiColor = "\u001B[91m";
                break;
            case "green_bright":
            case "bright_green":
                ansiColor = "\u001B[92m";
                break;
            case "yellow_bright":
            case "bright_yellow":
                ansiColor = "\u001B[93m";
                break;
            case "blue_bright":
            case "bright_blue":
                ansiColor = "\u001B[94m";
                break;
            case "magenta_bright":
            case "bright_magenta":
                ansiColor = "\u001B[95m";
                break;
            case "cyan_bright":
            case "bright_cyan":
                ansiColor = "\u001B[96m";
                break;
            case "white_bright":
            case "bright_white":
                ansiColor = "\u001B[97m";
                break;

            // Background colors
            case "black_bg":
            case "bg_black":
                ansiColor = "\u001B[40m";
                break;
            case "red_bg":
            case "bg_red":
                ansiColor = "\u001B[41m";
                break;
            case "green_bg":
            case "bg_green":
                ansiColor = "\u001B[42m";
                break;
            case "yellow_bg":
            case "bg_yellow":
                ansiColor = "\u001B[43m";
                break;
            case "blue_bg":
            case "bg_blue":
                ansiColor = "\u001B[44m";
                break;
            case "magenta_bg":
            case "bg_magenta":
                ansiColor = "\u001B[45m";
                break;
            case "cyan_bg":
            case "bg_cyan":
                ansiColor = "\u001B[46m";
                break;
            case "white_bg":
            case "bg_white":
                ansiColor = "\u001B[47m";
                break;

            default:
                ansiColor = "";
                break;
        }
        return ansiColor;
    }

    protected static void enableColorsForWindows() {
        // Set output mode to handle virtual terminal sequences
        Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
        WinDef.DWORD STD_OUTPUT_HANDLE = new WinDef.DWORD(-11);
        WinNT.HANDLE hOut = (WinNT.HANDLE) GetStdHandleFunc.invoke(WinNT.HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

        WinDef.DWORDByReference p_dwMode = new WinDef.DWORDByReference(new WinDef.DWORD(0));
        Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
        GetConsoleModeFunc.invoke(WinDef.BOOL.class, new Object[]{hOut, p_dwMode});

        int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
        WinDef.DWORD dwMode = p_dwMode.getValue();
        dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
        Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
        SetConsoleModeFunc.invoke(WinDef.BOOL.class, new Object[]{hOut, dwMode});
    }

    public interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        boolean SetConsoleOutputCP(int codePage);
        boolean SetConsoleTitleA(String lpConsoleTitle);
    }

    /**
     * Enables UTF-8 encoding for the current console by calling SetConsoleOutputCP.
     */
    public static void utf8enable() throws UnsupportedEncodingException {
        // Устанавливаем кодовую страницу UTF-8 для текущей консоли
        Kernel32.INSTANCE.SetConsoleOutputCP(65001);
        // Переназначаем стандартные потоки вывода с указанием кодировки UTF-8
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the title of the console window.
     */
    public static void setwindowtitle() {
        Kernel32.INSTANCE.SetConsoleTitleA("TulparServer");
    }

    public static void checkServerAvailability() {
        if (isClassAvailable("org.eclipse.jetty.server.Server")) {
            Logger.info("Server API is available");
            return;
        }
        loadJarsFromFolder("./libs");
        if (isClassAvailable("org.eclipse.jetty.server.Server")) {
            return;
        }
        System.exit(1);
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void loadJarsFromFolder(String folderPath) {
        File libsDir = new File(folderPath);
        if (!libsDir.exists() || !libsDir.isDirectory()) {
            return;
        }
        File[] jarFiles = libsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            return;
        }
        try {
            // Получаем системный загрузчик классов
            URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> sysClass = URLClassLoader.class;
            // Через рефлексию получаем доступ к методу addURL
            java.lang.reflect.Method addUrlMethod = sysClass.getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);
            // Добавляем каждый jar в системный classloader
            for (File jar : jarFiles) {
                Logger.devinfo("Loaded: " + jar.getName());
                URL jarUrl = jar.toURI().toURL();
                addUrlMethod.invoke(sysLoader, jarUrl);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}