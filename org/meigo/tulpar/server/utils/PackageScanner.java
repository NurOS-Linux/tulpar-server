package org.meigo.tulpar.server.utils;

import org.python.util.PythonInterpreter;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageScanner {
    private static final String PACKAGES_DIR = "./packages";
    private static final String UNPACKER_SCRIPT = "apgunpacker.py"; // Без packages

    public static void scanAndUnpack(PythonInterpreter pyInterp) {
        System.out.println("[LOG] Запуск сканирования папки " + PACKAGES_DIR);

        File packagesDir = new File(PACKAGES_DIR);
        if (!packagesDir.exists() || !packagesDir.isDirectory()) {
            System.out.println("[ERROR] Директория " + packagesDir.getAbsolutePath() + " не найдена.");
            return;
        }

        File[] files = packagesDir.listFiles((dir, name) -> name.endsWith(".apg"));
        if (files == null || files.length == 0) {
            System.out.println("[LOG] Нет .apg файлов для обработки.");
            return;
        }

        System.out.println("[LOG] Найдено " + files.length + " .apg файлов.");

        for (File apgFile : files) {
            System.out.println("[LOG] Обрабатываем файл: " + apgFile.getName());

            String packageName = extractPackageName(apgFile.getName());
            if (packageName == null) {
                System.out.println("[ERROR] Не удалось извлечь имя пакета из " + apgFile.getName());
                continue;
            }

            File packageDir = new File(packagesDir, packageName);
            if (packageDir.exists()) {
                System.out.println("[LOG] Пропускаем " + apgFile.getName() + " (папка уже существует)");
                continue;
            }

            unpackPackage(pyInterp, apgFile);
        }
    }

    private static String extractPackageName(String fileName) {
        Matcher matcher = Pattern.compile("^[^-_]+").matcher(fileName);
        return matcher.find() ? matcher.group() : null;
    }

    private static void unpackPackage(PythonInterpreter pyInterp, File apgFile) {
        System.out.println("[LOG] Начинаем распаковку: " + apgFile.getName());

        String pythonCmd = GetPythonCommand.getName();
        if (pythonCmd == null) {
            System.out.println("[ERROR] Python не найден, пропускаем " + apgFile.getName());
            return;
        }

        try {
            // Теперь скрипт ищем в корне, а не в packages
            String scriptPath = new File(UNPACKER_SCRIPT).getCanonicalPath().replace("\\", "/");
            String apgPath = apgFile.getCanonicalPath().replace("\\", "/");

            String command = pythonCmd + " \"" + scriptPath + "\" \"" + apgPath + "\"";
            String execCommand = "import os; os.system('" + command + "')";

            System.out.println("[LOG] Запуск: " + command);
            pyInterp.exec(execCommand);
        } catch (IOException e) {
            System.err.println("[ERROR] Ошибка получения пути: " + e.getMessage());
        }
    }
}
