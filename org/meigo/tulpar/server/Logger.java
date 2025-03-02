package org.meigo.tulpar.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    // ANSI escape codes
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_CYAN   = "\u001B[36m"; // devinfo
    private static final String ANSI_YELLOW = "\u001B[33m"; // warn
    private static final String ANSI_RED    = "\u001B[31m"; // error
    private static final String ANSI_GREEN  = "\u001B[32m"; // success

    // Определяем класс вызывающего кода
    private static String getCallerClassName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String loggerClassName = Logger.class.getName();
        // Перебираем элементы стека, пропуская вызовы из Logger и Thread
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (!className.equals(loggerClassName) && !className.equals(Thread.class.getName())) {
                return className;
            }
        }
        return "Unknown";
    }

    // Получаем текущее время в формате HH:mm:ss
    private static String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Форматирование сообщения
    private static String format(String message) {
        return "[" + getCallerClassName() + "] (" + getCurrentTime() + ") " + message;
    }

    public static void devinfo(String message) {
        System.out.println(ANSI_CYAN + format(message) + ANSI_RESET);
    }

    public static void warn(String message) {
        System.out.println(ANSI_YELLOW + format(message) + ANSI_RESET);
    }

    public static void error(String message) {
        System.out.println(ANSI_RED + format(message) + ANSI_RESET);
    }

    public static void success(String message) {
        System.out.println(ANSI_GREEN + format(message) + ANSI_RESET);
    }

    public static void info(String message) {
        System.out.println(format(message));
    }
}
