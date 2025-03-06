package org.meigo.tulpar.server.utils;

import org.meigo.tulpar.server.Config;
import org.meigo.tulpar.server.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс RequestLimiter реализует механизм ограничения количества запросов от одного IP-адреса
 * с дополнительными мерами защиты от DoS/DDoS-атак.
 *
 * Основные функции класса:
 *
 *   Лимитирование количества запросов в заданном временном интервале (например, 60 секунд).
 *   Динамическая блокировка IP-адресов, превышающих лимит, с автоматическим снятием блокировки по истечении заданного времени.
 *   Подробное логирование входящих запросов и действий по блокировке для последующего аудита и анализа аномалий.
 *
 *
 * Для конфигурации используются следующие параметры из класса Config:
 *
 *   Config.servermaxRequests – максимальное количество запросов от одного IP за 60 секунд;
 *   Config.blockDurationMillis – время блокировки IP в миллисекундах после превышения лимита.
 *
 *
 * Класс предназначен для вызова в каждом HTTP-сервлете (например, в методе doGet()) перед обработкой запроса.
 *
 * Дополнительно: В будущем можно интегрировать данный механизм с системами мониторинга (например, SIEM)
 * для централизованного анализа аномальной активности.
 */
public class RequestLimiter {
    /**
     * Карта для хранения меток времени запросов по IP.
     *
     * Ключ: IP-адрес клиента.
     * Значение: Синхронизированный список временных меток (в миллисекундах), отражающих время получения запроса.
     */
    private static final Map<String, List<Long>> requestsMap = new ConcurrentHashMap<>();

    /**
     * Карта для хранения информации о временной блокировке IP-адресов.
     *
     * Ключ: IP-адрес клиента.
     * Значение: Время (timestamp в миллисекундах), до которого IP остается заблокированным.
     */
    private static final Map<String, Long> blockedIPs = new ConcurrentHashMap<>();

    /**
     * Проверяет допустимость обработки входящего HTTP-запроса.
     *
     * Алгоритм работы метода:
     *
     *   Логирование входящего запроса с указанием IP-адреса и запрошенного URI.
     *   Проверка, находится ли IP в списке заблокированных:
     *
     *       Если IP заблокирован и время блокировки еще не истекло, метод возвращает false и отправляет ответ с сообщением об ошибке.
     *       Если время блокировки истекло, IP автоматически разблокируется.
     *
     *
     *   Получение списка временных меток для данного IP (или создание нового, если он отсутствует).
     *   Удаление из списка записей, старше 60 секунд, для актуализации данных.
     *   Если количество запросов за последний интервал превышает лимит Config.servermaxRequests:
     *
     *       IP заносится в список заблокированных с указанием времени окончания блокировки (now + Config.blockDurationMillis).
     *       Отправляется сообщение о временной блокировке и метод возвращает false.
     *
     *
     *   Если лимит не превышен, регистрируется текущий запрос, и метод возвращает true для дальнейшей обработки.
     *
     * @param request  HTTP-запрос, полученный от клиента.
     * @param response HTTP-ответ, предназначенный для отправки клиенту.
     * @return true, если запрос разрешено обрабатывать; false – если превышен лимит или IP заблокирован.
     * @throws IOException при ошибках записи в ответ.
     */
    public static boolean checkRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        long now = System.currentTimeMillis();

        // Логирование входящего запроса.
        Logger.devinfo(ip + " зашел на " + path);

        // Проверка, заблокирован ли IP-адрес.
        if (blockedIPs.containsKey(ip)) {
            long blockExpiry = blockedIPs.get(ip);
            if (now < blockExpiry) {
                response.getWriter().write("IP is temporarily blocked due to excessive requests");
                Logger.devinfo("Блокировка для " + ip + " действует до " + new Date(blockExpiry));
                return false;
            } else {
                // Если время блокировки истекло, удаляем IP из списка заблокированных.
                blockedIPs.remove(ip);
                Logger.devinfo("Снятие блокировки с " + ip);
            }
        }

        // Получение или создание списка временных меток запросов для данного IP.
        List<Long> timestamps = requestsMap.computeIfAbsent(ip, k -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (timestamps) {
            // Удаление запросов, поступивших более 60 секунд назад.
            timestamps.removeIf(timestamp -> now - timestamp > 60000);

            // Если количество запросов за последние 60 секунд превышает заданный лимит...
            if (timestamps.size() >= Config.servermaxRequests) {
                // Рассчитываем время окончания блокировки.
                long blockUntil = now + Config.blockDurationMillis;
                blockedIPs.put(ip, blockUntil);
                response.getWriter().write("IP is temporarily blocked due to excessive requests");
                Logger.warn("IP " + ip + " заблокирован до " + new Date(blockUntil) + " из-за превышения лимита запросов");
                return false;
            }
            // Регистрируем текущий запрос.
            timestamps.add(now);
        }

        return true;
    }

    /**
     * Ручное блокирование указанного IP-адреса.
     *
     * Используется для административного управления (например, через командный интерфейс сервера).
     *
     * @param ip IP-адрес, который необходимо заблокировать.
     */
    public static void blockIP(String ip) {
        long blockUntil = System.currentTimeMillis() + Config.blockDurationMillis;
        blockedIPs.put(ip, blockUntil);
        Logger.devinfo("IP " + ip + " вручную заблокирован до " + new Date(blockUntil));
    }

    /**
     * Ручное снятие блокировки с указанного IP-адреса.
     *
     * Используется для административного управления (например, через командный интерфейс сервера).
     *
     * @param ip IP-адрес, с которого необходимо снять блокировку.
     */
    public static void unblockIP(String ip) {
        blockedIPs.remove(ip);
        Logger.devinfo("Блокировка для IP " + ip + " вручную снята");
    }

    /**
     * Возвращает карту всех заблокированных IP-адресов с указанием времени окончания блокировки.
     *
     * Данный метод может быть полезен для мониторинга состояния системы или отображения информации
     * в административном интерфейсе.
     *
     * @return Карта заблокированных IP-адресов, где ключ – IP-адрес, а значение – время окончания блокировки.
     */
    public static Map<String, Long> getBlockedIPs() {
        return blockedIPs;
    }
}
