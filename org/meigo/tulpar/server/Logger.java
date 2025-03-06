package org.meigo.tulpar.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс Logger предоставляет методы для логирования сообщений с различными уровнями:
 * devinfo, warn, error, success, info.
 *
 * Дополнительная функциональность:
 * - Если одно и то же сообщение (определяемое по ключу: уровень + вызывающий класс + сообщение)
 *   логируется несколько раз в течение 1 секунды, вместо вывода повторных строк,
 *   предыдущая строка обновляется, добавляя суффикс " xN", где N – число повторов.
 * - Кэширование сообщений производится в течение 1 минуты, после чего устаревшие записи удаляются.
 *
 * Для консольного вывода используется ANSI-цветовая раскраска.
 */
public class Logger {
    // ANSI escape codes для раскраски сообщений
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_CYAN   = "\u001B[36m"; // devinfo
    private static final String ANSI_YELLOW = "\u001B[33m"; // warn
    private static final String ANSI_RED    = "\u001B[31m"; // error
    private static final String ANSI_GREEN  = "\u001B[32m"; // success

    // Объект для синхронизации операций логирования
    private static final Object lock = new Object();

    // Кэш для хранения агрегированных лог-сообщений.
    // Ключ: "LEVEL:вызывающийКласс:сообщение", значение: объект с данными о повторениях.
    private static final Map<String, LogCacheEntry> logCache = new ConcurrentHashMap<>();

    /**
     * Вспомогательный класс для хранения информации о повторяющемся лог-сообщении.
     */
    private static class LogCacheEntry {
        int count;              // Количество повторов
        long lastTimestamp;     // Время последнего логирования (в мс)
        String baseMessage;     // Форматированное базовое сообщение (без суффикса xN)
    }

    /**
     * Возвращает имя класса, вызвавшего метод логирования.
     *
     * @return Имя вызывающего класса.
     */
    private static String getCallerClassName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String loggerClassName = Logger.class.getName();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (!className.equals(loggerClassName) && !className.equals(Thread.class.getName())) {
                return className;
            }
        }
        return "Unknown";
    }

    /**
     * Возвращает текущее время в формате HH:mm:ss.
     *
     * @return Текущее время в виде строки.
     */
    private static String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Форматирует сообщение для логирования, добавляя имя вызывающего класса и текущее время.
     *
     * @param message Исходное сообщение.
     * @return Отформатированное сообщение.
     */
    private static String format(String message) {
        return "[" + getCallerClassName() + "] (" + getCurrentTime() + ") " + message;
    }

    /**
     * Основной метод логирования, реализующий проверку повторов.
     *
     * Алгоритм:
     * 1. Формируется ключ: уровень + ":" + вызывающий класс + ":" + исходное сообщение.
     * 2. Выполняется очистка кэша: из него удаляются записи старше 1 минуты.
     * 3. Если по ключу уже есть запись и время последнего логирования не превышает 1 секунду,
     *    обновляется счетчик, и предыдущая строка перезаписывается с добавлением суффикса " x<count>".
     * 4. Иначе, если записи нет или прошло больше 1 секунды, выводится новое сообщение,
     *    и создаётся/обновляется запись в кэше.
     *
     * @param level   Уровень логирования (например, "DEVINFO", "WARN", "ERROR", "SUCCESS", "INFO").
     * @param message Исходное сообщение.
     * @param color   ANSI-код цвета для вывода.
     */
    private static void logMessage(String level, String message, String color) {
        synchronized (lock) {
            long now = System.currentTimeMillis();

            // Очистка кэша: удаляем записи старше 1 минуты (60000 мс)
            Iterator<Map.Entry<String, LogCacheEntry>> iterator = logCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, LogCacheEntry> entry = iterator.next();
                if (now - entry.getValue().lastTimestamp > 60000) {
                    iterator.remove();
                }
            }

            String caller = getCallerClassName();
            String key = level + ":" + caller + ":" + message;
            String baseFormatted = "[" + caller + "] (" + getCurrentTime() + ") " + message;
            LogCacheEntry entry = logCache.get(key);

            if (entry != null && now - entry.lastTimestamp <= 1000) {
                // Если сообщение повторяется в пределах 1 секунды — обновляем счетчик
                entry.count++;
                entry.lastTimestamp = now;
                // Формируем обновленное сообщение с суффиксом x<count>
                String updated = entry.baseMessage + " x" + entry.count;
                // Обновляем предыдущую строку (используем carriage return)
                System.out.print("\r" + color + updated + ANSI_RESET);
                System.out.flush();
            } else {
                // Если либо записи нет, либо прошло более 1 секунды с предыдущего логирования:
                // Если была предыдущая запись, завершаем её переносом строки
                if (entry != null) {
                    System.out.println();
                }
                // Создаем новую запись в кэше
                LogCacheEntry newEntry = new LogCacheEntry();
                newEntry.count = 1;
                newEntry.lastTimestamp = now;
                newEntry.baseMessage = baseFormatted;
                logCache.put(key, newEntry);
                // Выводим новое сообщение с переводом строки
                System.out.println(color + baseFormatted + ANSI_RESET);
            }
        }
    }

    // Методы логирования с разными уровнями

    public static void devinfo(String message) {
        logMessage("DEVINFO", message, ANSI_CYAN);
    }

    public static void warn(String message) {
        logMessage("WARN", message, ANSI_YELLOW);
    }

    public static void error(String message) {
        logMessage("ERROR", message, ANSI_RED);
    }

    public static void success(String message) {
        logMessage("SUCCESS", message, ANSI_GREEN);
    }

    public static void info(String message) {
        logMessage("INFO", message, "");
    }
}
