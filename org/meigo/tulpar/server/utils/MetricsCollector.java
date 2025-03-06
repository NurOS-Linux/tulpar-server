package org.meigo.tulpar.server.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.meigo.tulpar.server.utils.RequestLimiter;

/**
 * Класс MetricsCollector реализует сбор метрик системы с использованием стандартного API JMX.
 *
 * Основные задачи класса:
 * - Периодически (каждые 5 минут) собирать статистику по использованию процессора, памяти и количеству забаненных IP.
 * - Формировать отчёт с собранными данными в одну строку и выводить его в консоль.
 * - Возможность расширить функциональность (например, отправка метрик на сервер мониторинга или сохранение в базу данных).
 *
 * Код кроссплатформенный, так как использует стандартное API Java.
 *
 * Использование:
 *  - Для запуска мониторинга вызовите MetricsCollector.startMonitoring() из вашего метода main.
 *  - При завершении работы приложения вызовите MetricsCollector.stopMonitoring() для корректного завершения планировщика.
 */
public class MetricsCollector {

    // Планировщик задач для периодического выполнения сбора метрик.
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Запускает сбор метрик.
     *
     * Метод создаёт задачу, которая каждые 5 минут:
     * - Собирает данные о загрузке CPU, использовании Heap и Non-Heap памяти, а также количестве забаненных IP.
     * - Формирует отчёт с метриками в одну строку.
     * - Выводит отчёт в консоль.
     */
    public static void startMonitoring() {
        Runnable collectMetricsTask = new Runnable() {
            @Override
            public void run() {
                try {
                    // Получение информации о памяти через MemoryMXBean.
                    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                    long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
                    long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
                    long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();

                    // Получение информации о системе через OperatingSystemMXBean.
                    java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
                    double cpuLoad = -1;
                    if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                        cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad();
                    }
                    // Если загрузка недоступна, выводим "Unavailable" вместо N/A.
                    String cpuLoadStr = (cpuLoad >= 0) ? String.format("%.2f%%", cpuLoad * 100) : "Unavailable";

                    // Получение количества забаненных IP через RequestLimiter.
                    int bannedIpCount = RequestLimiter.getBlockedIPs().size();

                    // Формирование отчёта в одну строку.
                    String report = "Metrics Report (" + new Date() + "): CPU Load: " + cpuLoadStr +
                            "; Banned IPs: " + bannedIpCount +
                            "; Heap Memory: " + formatBytes(heapUsed) + " used / " + formatBytes(heapMax) +
                            "; Non-Heap Memory: " + formatBytes(nonHeapUsed) + " used.";

                    // Вывод отчёта в консоль.
                    System.out.println(report);

                } catch (Exception e) {
                    System.err.println("Error collecting metrics: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        // Запуск задачи немедленно, затем каждые 5 минут.
        scheduler.scheduleAtFixedRate(collectMetricsTask, 0, 5, TimeUnit.MINUTES);
        System.out.println("Metrics monitoring started. Reports will be generated every 5 minutes.");
    }

    /**
     * Останавливает мониторинг, корректно завершая работу планировщика задач.
     */
    public static void stopMonitoring() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Metrics monitoring stopped.");
    }

    /**
     * Вспомогательный метод для форматирования размера в байтах в удобочитаемый формат.
     * Примеры: 1024 B, 1.0 KB, 10.0 MB, 1.0 GB и т.д.
     *
     * @param bytes число байтов
     * @return строка с отформатированным значением размера
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
