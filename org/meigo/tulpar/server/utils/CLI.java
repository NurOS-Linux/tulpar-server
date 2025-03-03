package org.meigo.tulpar.server.utils;

import org.meigo.tulpar.server.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CLI {

    // Флаг работы CLI
    private static boolean running = true;

    // Карта для хранения зарегистрированных команд (имя команды -> метод)
    private static Map<String, Method> commands = new HashMap<>();

    // Статический блок для автоматической регистрации методов-команд
    static {
        // Ищем все методы в данном классе, начинающиеся с "cmd" и не принимающие аргументов
        for (Method method : CLI.class.getDeclaredMethods()) {
            if (method.getName().startsWith("cmd") && method.getParameterCount() == 0) {
                // Имя команды – это имя метода без префикса "cmd", преобразованное в нижний регистр
                String commandName = method.getName().substring(3).toLowerCase();
                commands.put(commandName, method);
                Logger.info("Зарегистрирована команда: " + commandName + " -> метод: " + method.getName());
            }
        }
    }

    // Метод для инициализации CLI
    public static void init() {
        Logger.info("CLI инициализирован.");
        System.out.println("CLI инициализирован. Введите команду:");
        Scanner scanner = new Scanner(System.in);

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            // Разбиваем ввод на слова: первая часть – название команды, остальные можно использовать как аргументы
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            Method method = commands.get(command);
            if (method != null) {
                try {
                    Logger.info("Вызов команды: " + command);
                    method.invoke(null); // вызов статического метода без аргументов
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.error("Ошибка при выполнении команды " + command + ": " + e.getMessage());
                    System.out.println("Ошибка при выполнении команды " + command + ": " + e.getMessage());
                }
            } else {
                Logger.warn("Неизвестная команда: " + command);
                System.out.println("Неизвестная команда: " + command);
            }
        }
        scanner.close();
        Logger.info("CLI завершён.");
        System.out.println("CLI завершён.");
    }

    // Команда для выхода из CLI
    public static void cmdshutdown() {
        Logger.devinfo("Команда exit вызвана.");
        running = false;
        System.exit(0);
    }

    // Пример дополнительной команды: вывод справки
    public static void cmdhelp() {
        Logger.success("Доступные команды:");
        for (String cmd : commands.keySet()) {
            Logger.success("  " + cmd);
        }
        Logger.devinfo("Команда help вызвана.");
    }
}
