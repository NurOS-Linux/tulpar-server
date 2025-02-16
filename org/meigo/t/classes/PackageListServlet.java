package org.meigo.t.classes;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PackageListServlet extends HttpServlet {
    private static final String PACKAGES_DIR = "package";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File packagesDir = new File(PACKAGES_DIR);
        if (!packagesDir.exists() || !packagesDir.isDirectory()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Packages directory not found.");
            return;
        }

        // Структура данных для вывода
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

        // Преобразование объекта в JSON с использованием Gson
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(response);

        resp.setContentType("application/json");
        resp.getWriter().write(jsonResponse);
    }

    // Вспомогательные классы для структуры JSON
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


