package org.meigo.tulpar.server.utils;

import org.meigo.tulpar.server.Config;
import org.meigo.tulpar.server.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestLimiter {
    // Карта для хранения меток времени запросов по IP
    private static final Map<String, List<Long>> requestsMap = new ConcurrentHashMap<>();
    // Набор IP, заблокированных вручную (CLI)
    private static final Set<String> blockedIPs = Collections.synchronizedSet(new HashSet<>());

    /**
     * Проверяет запрос по IP:
     * - Логгирует запрос: IP зашел на определённый путь.
     * - Если IP в списке заблокированных, сразу отвечает сообщением.
     * - Удаляет из списка запросов, старше 60 секунд.
     * - Если число запросов за последнюю минуту превышает Config.servermaxRequests, отвечает сообщением.
     *
     * @param request  запрос от клиента
     * @param response ответ клиенту
     * @return true, если запрос можно обрабатывать, false – если заблокирован
     * @throws IOException при ошибке записи в ответ
     */
    public static boolean checkRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        // Логируем, какой IP зашел на какой путь
        Logger.devinfo(ip + " зашел на " + path);

        // Если IP заблокирован вручную
        if (blockedIPs.contains(ip)) {
            response.getWriter().write("IP is temporarily blocked");
            return false;
        }

        long now = System.currentTimeMillis();
        // Получаем список меток времени запросов для IP, либо создаём новый список
        List<Long> timestamps = requestsMap.computeIfAbsent(ip, k -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (timestamps) {
            // Удаляем записи старше 60 секунд
            timestamps.removeIf(timestamp -> now - timestamp > 60000);
            // Если количество запросов превышает лимит, блокируем запрос
            if (timestamps.size() >= Config.servermaxRequests) {
                response.getWriter().write("IP is temporarily blocked");
                return false;
            }
            // Регистрируем текущий запрос
            timestamps.add(now);
        }
        return true;
    }

    // Метод для ручной блокировки IP
    public static void blockIP(String ip) {
        blockedIPs.add(ip);
    }

    // Метод для ручной разблокировки IP
    public static void unblockIP(String ip) {
        blockedIPs.remove(ip);
    }

    // Метод для получения списка заблокированных IP (если потребуется)
    public static Set<String> getBlockedIPs() {
        return blockedIPs;
    }
}
