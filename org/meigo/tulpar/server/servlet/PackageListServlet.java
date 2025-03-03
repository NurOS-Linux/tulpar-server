package org.meigo.tulpar.server.servlet;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.meigo.tulpar.server.utils.RequestLimiter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class PackageListServlet extends HttpServlet {
    private static final String PACKAGES_DIR = "package";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Ограничение по количеству запросов, блокировка по IP и логирование запроса
        if (!RequestLimiter.checkRequest(req, resp)) {
            return;
        }

        String pkg = req.getParameter("package");
        if (pkg != null && !pkg.isEmpty()) {
            // Если указан параметр package – формируем детальный ответ
            PackageDetailResponse detailResponse = new PackageDetailResponse();
            detailResponse.packageName = pkg;
            detailResponse.architectures = new HashMap<>();

            File packagesDir = new File(PACKAGES_DIR);
            if (!packagesDir.exists() || !packagesDir.isDirectory()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Packages directory not found.");
                return;
            }

            File[] packageDirs = packagesDir.listFiles(File::isDirectory);
            if (packageDirs != null) {
                for (File packageDir : packageDirs) {
                    // Ищем файл metadata.json в каждом подкаталоге
                    File metadataFile = new File(packageDir, "metadata.json");
                    if (!metadataFile.exists() || !metadataFile.isFile()) {
                        continue;
                    }
                    // Парсинг metadata.json
                    try (Reader reader = new FileReader(metadataFile)) {
                        Gson gson = new Gson();
                        Metadata metadata = gson.fromJson(reader, Metadata.class);
                        // Если имя пакета (из metadata) совпадает с параметром запроса (без учёта регистра)
                        if (metadata.name != null && metadata.name.equalsIgnoreCase(pkg)) {
                            String arch = metadata.architecture;
                            String versionRelease = metadata.version + "-" + metadata.release;
                            String downloadUrl = buildDownloadUrl(metadata);

                            // Группируем пакеты по архитектурам
                            ArchitectureInfo archInfo = detailResponse.architectures.get(arch);
                            double currentScore = computeVersionScore(metadata.version, metadata.release);
                            if (archInfo == null) {
                                archInfo = new ArchitectureInfo();
                                archInfo.latest = versionRelease;
                                archInfo.latestScore = currentScore;
                                archInfo.versions = new HashMap<>();
                                archInfo.versions.put(versionRelease, downloadUrl);
                                detailResponse.architectures.put(arch, archInfo);
                            } else {
                                archInfo.versions.put(versionRelease, downloadUrl);
                                if (currentScore > archInfo.latestScore) {
                                    archInfo.latestScore = currentScore;
                                    archInfo.latest = versionRelease;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // В случае ошибки парсинга можно залогировать исключение
                        e.printStackTrace();
                    }
                }
            }
            // Отдаём сформированный объект в JSON
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(detailResponse);
            resp.setContentType("application/json");
            resp.getWriter().write(jsonResponse);
        } else {
            // Если параметр package не указан – старая логика вывода списка пакетов
            File packagesDir = new File(PACKAGES_DIR);
            if (!packagesDir.exists() || !packagesDir.isDirectory()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Packages directory not found.");
                return;
            }

            PackageListResponse response = new PackageListResponse();
            File[] packageDirs = packagesDir.listFiles(File::isDirectory);
            if (packageDirs != null) {
                for (File packageDir : packageDirs) {
                    PackageInfo packageInfo = new PackageInfo(packageDir.getName());
                    File[] files = packageDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            packageInfo.addFile(file.getName(), file.isDirectory());
                        }
                    }
                    response.addPackage(packageInfo);
                }
            }
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(response);
            resp.setContentType("application/json");
            resp.getWriter().write(jsonResponse);
        }
    }

    // Вычисляет оценочное значение версии для сравнения
    private double computeVersionScore(String version, int release) {
        try {
            return Double.parseDouble(version) * 1000 + release;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Формирует URL для скачивания пакета
    private String buildDownloadUrl(Metadata metadata) {
        String versionRelease = metadata.version + "-" + metadata.release;
        return "repo.nuros.org/packages/" + metadata.name + "-" + versionRelease + "-" + metadata.architecture + ".apg";
    }

    // Класс для формирования детального ответа по пакету
    private static class PackageDetailResponse {
        @SerializedName("package")
        String packageName;
        Map<String, ArchitectureInfo> architectures;
    }

    // Класс для сведений по конкретной архитектуре
    private static class ArchitectureInfo {
        String latest;
        Map<String, String> versions;
        transient double latestScore; // не сериализуется
    }

    // Класс для маппинга metadata.json
    private static class Metadata {
        String name;
        String version;
        int release;
        String architecture;
        // Дополнительные поля можно добавить при необходимости
    }

    // Старая логика формирования списка пакетов
    private static class PackageListResponse {
        private final List<PackageInfo> packages = new ArrayList<>();

        public void addPackage(PackageInfo packageInfo) {
            packages.add(packageInfo);
        }
    }

    private static class PackageInfo {
        private final String name;
        private final Map<String, String> files = new HashMap<>();

        public PackageInfo(String name) {
            this.name = name;
        }

        public void addFile(String fileName, boolean isDirectory) {
            files.put(fileName, isDirectory ? "directory" : "file");
        }
    }
}
