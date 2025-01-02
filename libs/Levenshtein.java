package libs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Levenshtein {

    public static int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length(); // мотематико
        int len2 = str2.length();
        int[][] matrix = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            matrix[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            matrix[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                matrix[i][j] = Math.min(Math.min(
                    matrix[i - 1][j] + 1, 
                    matrix[i][j - 1] + 1),
                    matrix[i - 1][j - 1] + cost
                );
            }
        }
        return matrix[len1][len2];
    }

    public static String findClosestCommand(String line, JSONObject commands) {
        String closestCommand = "";
        int shortestDistance = Integer.MAX_VALUE; // 2 лярда

        for (String key : commands.keySet()) {
            int distance = levenshteinDistance(line, key);
            if (distance < shortestDistance) {
                closestCommand = key;
                shortestDistance = distance;
            }
        }
        return closestCommand;
    }

    public static void main(String[] args) {
        String jsonString = "";
        try {
            jsonString = new String(Files.readAllBytes(Paths.get("commands.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray commandsArray = new JSONArray(jsonString);
        JSONObject commands = commandsArray.getJSONObject(0); // commands.json чекай, там такая же структура должна быть
        String line = "hlp"; // выведет help, написав endd выведет end
        String closestCommand = findClosestCommand(line, commands);
        System.out.println("Самая похожая команда: " + closestCommand);
    }
}
