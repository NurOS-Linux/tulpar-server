package org.meigo.tulpar.server.utils;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.meigo.tulpar.server.Config;
import org.meigo.tulpar.server.Logger;
import org.meigo.tulpar.server.TServer;

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
        // Регистрируем методы, имя которых начинается с "cmd" и которые либо не принимают аргументов,
        // либо принимают единственный аргумент типа String[]
        for (Method method : CLI.class.getDeclaredMethods()) {
            if (method.getName().startsWith("cmd")) {
                int paramCount = method.getParameterCount();
                if (paramCount == 0 || (paramCount == 1 && method.getParameterTypes()[0].equals(String[].class))) {
                    String commandName = method.getName().substring(3).toLowerCase();
                    commands.put(commandName, method);
                    Logger.info("Зарегистрирована команда: " + commandName + " -> метод: " + method.getName());
                }
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

            // Разбиваем ввод на слова: первая часть – название команды, остальные – аргументы
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            String[] args = new String[parts.length - 1];
            if (parts.length > 1) {
                System.arraycopy(parts, 1, args, 0, parts.length - 1);
            }

            Method method = commands.get(command);
            if (method != null) {
                try {
                    Logger.info("Вызов команды: " + command);
                    if (method.getParameterCount() == 0) {
                        method.invoke(null);
                    } else {
                        // Передаём аргументы, используя приведение типа для varargs
                        method.invoke(null, (Object) args);
                    }
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
    public static void cmdshutdown() throws Exception {
        Logger.devinfo("Команда shutdown вызвана.");
        running = false;
        TServer.tserver.stop();
    }

    public static void cmdgetRequestLog() throws Exception {
        InMemoryRequestLog log = TServer.getRequestLog();
        if (log != null) {
            Logger.info(log.toString());
        } else {
            Logger.warn("Лог запросов не инициализирован.");
        }
    }


    // Команда для настройки SSL с передачей аргументов
    // Ожидает три аргумента: keystorePath, keystorePassword, keyManagerPassword
    public static void cmdsetupSSL(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Использование: setupssl <keystorePath> <keystorePassword> <keyManagerPassword>");
            return;
        }
        String keystorePath = args[0];
        String keystorePassword = args[1];
        String keyManagerPassword = args[2];

        TServer.tserver.stop();
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);

        ServerConnector sslConnector = new ServerConnector(TServer.tserver,
                new SslConnectionFactory(sslContextFactory, "http/1.1"));
        sslConnector.setPort(Config.serverport);
        // При необходимости можно добавить sslConnector к серверу, например:
        // TServer.tserver.addConnector(sslConnector);

        Logger.success("SSL настроен успешно с keystore: " + keystorePath);
        TServer.tserver.start();
        TServer.tserver.join();
    }

    // Команда вывода справки
    public static void cmdhelp() {
        Logger.success("Доступные команды:");
        for (String cmd : commands.keySet()) {
            Logger.success("  " + cmd);
        }
        Logger.devinfo("Команда help вызвана.");
    }
}
