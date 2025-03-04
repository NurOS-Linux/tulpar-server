package org.meigo.tulpar.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.Function;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import org.eclipse.jetty.server.Server;
import org.fusesource.jansi.AnsiConsole;
import org.meigo.tulpar.server.Config;
import org.python.util.PythonInterpreter;

import java.util.Objects;

import static org.meigo.tulpar.server.Config.config;
import static org.meigo.tulpar.server.api.python.pyInterp;
import static org.meigo.tulpar.server.sysinfo.*;


public class Main {

    public static long startTime = System.currentTimeMillis();


    public static void main(String[] args) throws Exception {
        // sysinfo
        sysinfo.init();
        Logger.devinfo("Init ...");
        Config.set("config.json");
        Config.load();

        Logger.info("Loading python 2.7 for plugins ...");

        pyInterp.exec("print('[org.meigo.tulpar.server.api.python] Loaded!')");



        // checking
        if (isSupportedArchitecture(arch)) {
            Logger.devinfo("The software is supported on this architecture: " + arch);
        } //else { Пока-что временно убрал, т.к. проверка пока что не нужна
        //    Logger.error("The software is not supported on this architecture: " + arch);
        //    System.exit(1);
        //}
        Logger.info("Server Initialisation ...");
        Config.apiconfig = Integer.parseInt(Objects.requireNonNull(Config.get("api.config")));
        Config.serveraddress = Config.get("server.address");
        Config.serverport = Integer.parseInt(Objects.requireNonNull(Config.get("server.port")));
        Config.serverrunInBackground = Config.get("server.runInBackground");
        Config.servermaxRequests = Integer.parseInt(Objects.requireNonNull(Config.get("server.maxRequests")));
        Config.serverlogFile = Config.get("server.logFile");
        Config.serverhttpsRedirect = Config.get("server.httpsRedirect").isEmpty();

        JsonObject cli = config.getAsJsonObject("cli");
        // Получаем строковое значение цвета и преобразуем его в ANSI-код
        String colorName = cli.get("color").getAsString();
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


        // Получаем массив "hello" и извлекаем первые две строки
        JsonArray helloArray = cli.getAsJsonArray("hello");
        String helloLine1 = helloArray.get(0).getAsString();
        String helloLine2 = helloArray.get(1).getAsString();

        // Выводим строки с заданным ANSI-цветом и сбросом цвета в конце
        System.out.println(ansiColor + helloLine1 + " " + arch + " | " + os + " | " + osver);
        System.out.println(ansiColor + helloLine2 + " BETA ver.3 " + "\u001B[0m");

        int width = AnsiConsole.getTerminalWidth();
        int oldwidth = width;
        if (width <= 0) { // если ширина не определена, устанавливаем значение по умолчанию
            width = 60;
        }
        System.out.println(ansiColor + repeat("=", width) + "\u001B[0m");

        Logger.warn("This is a test beta version of TulparServer. The product is not ready for full operation, there may be security issues.");

        if (oldwidth <= 0) {
            Logger.error("We were unable to determine the width of the terminal, is it definitely a terminal?");
        }

        TServer.start();
    }

    private static boolean isSupportedArchitecture(String arch) {
        return arch.equals("amd64") || arch.equals("x86_64") || arch.equals("i386") || arch.equals("i486") || arch.equals("i586") || arch.equals("i686") || arch.equals("x86");
    }

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
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

    public static void utf8enable() {
        pyInterp.exec("import os; os.system('chcp 65001')");
    }

    public static void setwindowtitle() {
        pyInterp.exec("import os; os.system('title TulparServer')");
    }
}