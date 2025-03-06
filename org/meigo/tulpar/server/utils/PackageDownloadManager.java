package org.meigo.tulpar.server.utils;

import org.meigo.tulpar.server.Config;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PackageDownloadManager {
    private static final Map<String, Integer> activeDownloads = new ConcurrentHashMap<>();
    private static final Map<String, Long> ipDownloadSpeeds = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastCheckTime = new ConcurrentHashMap<>();

    public static void downloadPackage(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String clientIp = req.getRemoteAddr();

        synchronized (activeDownloads) {
            int currentDownloads = activeDownloads.getOrDefault(clientIp, 0);
            if (currentDownloads >= Config.MAX_DOWNLOADS_PER_IP) {
                resp.sendError(429, "Maximum downloads reached.");
                return;
            }
            activeDownloads.put(clientIp, currentDownloads + 1);
        }

        resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        resp.setContentType("application/octet-stream");

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {

            byte[] buffer = new byte[Config.BUFFER_SIZE];
            int bytesRead;
            long startTime = System.currentTimeMillis();
            long bytesSent = 0;

            // Инициализация значений для IP
            ipDownloadSpeeds.putIfAbsent(clientIp, 0L);
            lastCheckTime.putIfAbsent(clientIp, System.currentTimeMillis());

            while ((bytesRead = fis.read(buffer)) != -1) {
                long now = System.currentTimeMillis();
                long elapsedTime = now - lastCheckTime.get(clientIp);

                if (elapsedTime >= 1000) {
                    ipDownloadSpeeds.put(clientIp, 0L);
                    lastCheckTime.put(clientIp, now);
                }

                long currentSpeed = ipDownloadSpeeds.get(clientIp);
                if (currentSpeed + bytesRead > Config.MAX_DOWNLOAD_SPEED) {
                    long sleepTime = 1000 - elapsedTime;
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    lastCheckTime.put(clientIp, System.currentTimeMillis());
                    ipDownloadSpeeds.put(clientIp, 0L);
                }

                ipDownloadSpeeds.put(clientIp, ipDownloadSpeeds.get(clientIp) + bytesRead);
                os.write(buffer, 0, bytesRead);
                os.flush();
                bytesSent += bytesRead;
            }
        } finally {
            synchronized (activeDownloads) {
                activeDownloads.put(clientIp, activeDownloads.get(clientIp) - 1);
                if (activeDownloads.get(clientIp) == 0) {
                    activeDownloads.remove(clientIp);
                    ipDownloadSpeeds.remove(clientIp);
                    lastCheckTime.remove(clientIp);
                }
            }
        }
    }
}
