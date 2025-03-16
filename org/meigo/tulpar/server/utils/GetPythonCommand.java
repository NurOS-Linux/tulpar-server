package org.meigo.tulpar.server.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetPythonCommand {
    public static String getName() {
        return isCommandAvailable("python3") ? "python3" :
                isCommandAvailable("python") ? "python" : null;
    }

    private static boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine() != null;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
