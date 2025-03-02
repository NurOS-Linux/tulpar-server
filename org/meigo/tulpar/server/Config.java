package org.meigo.tulpar.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static String filePath;
    public static JsonObject config;

    public static int apiconfig;
    public static String serveraddress;
    public static int serverport;
    public static String serverrunInBackground;
    public static int servermaxRequests;
    public static String serverlogFile;
    public static boolean serverhttpsRedirect;

    // Sets the configuration file path
    public static void set(String filePath) {
        Config.filePath = filePath;
        Logger.devinfo("Configuration initialized with file: " + filePath);
    }

    // Loads the configuration and validates the JSON format
    public static boolean load() {
        File file = new File(filePath);
        if (!file.exists()) {
            Logger.error("Configuration file not found: " + filePath);
            System.exit(1);
        }
        try (FileReader reader = new FileReader(file)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
            Logger.devinfo("Configuration loaded from file: " + filePath);
            return true;
        } catch (IOException e) {
            Logger.error("Error reading configuration file: " + filePath);
            System.exit(1);
        } catch (JsonSyntaxException e) {
            Logger.error("Invalid JSON in configuration file: " + filePath);
            System.exit(1);
        }
        return false; // Unreachable, but required by the compiler
    }

    // Retrieves the value for a given key in dot notation (e.g., "app.info")
    public static String get(String key) {
        Logger.devinfo("Retrieving value for key: " + key + " from configuration file: " + filePath);
        String[] keys = key.split("\\.");
        JsonObject current = config;
        for (int i = 0; i < keys.length - 1; i++) {
            if (current.has(keys[i]) && current.get(keys[i]).isJsonObject()) {
                current = current.getAsJsonObject(keys[i]);
            } else {
                Logger.error("Key not found: " + key);
                System.exit(1);
            }
        }
        if (!current.has(keys[keys.length - 1])) {
            Logger.error("Key not found: " + key);
            System.exit(1);
        }
        return current.get(keys[keys.length - 1]).getAsString();
    }

    // Sets a new value for a given key in dot notation (e.g., "app.info")
    public static void set(String key, String value) {
        Logger.devinfo("Setting new value: " + value + " for key: " + key + " in configuration file: " + filePath);
        String[] keys = key.split("\\.");
        JsonObject current = config;
        for (int i = 0; i < keys.length - 1; i++) {
            if (!current.has(keys[i]) || !current.get(keys[i]).isJsonObject()) {
                JsonObject newObj = new JsonObject();
                current.add(keys[i], newObj);
                current = newObj;
            } else {
                current = current.getAsJsonObject(keys[i]);
            }
        }
        current.addProperty(keys[keys.length - 1], value);
    }

    // Saves the configuration back to the file
    public static boolean save() {
        try (FileWriter writer = new FileWriter(filePath)) {
            new Gson().toJson(config, writer);
            Logger.devinfo("Configuration saved to file: " + filePath);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to save configuration to file: " + filePath);
            e.printStackTrace();
            return false;
        }
    }
}
