package meigo;

import java.util.HashMap;
import java.util.Map;

public class Colors {
    public static final Map<String, String> ANSI_CODES = new HashMap<>();

    static {
        ANSI_CODES.put("off", "0");
        ANSI_CODES.put("bold", "1");
        ANSI_CODES.put("italic", "3");
        ANSI_CODES.put("underline", "4");
        ANSI_CODES.put("blink", "5");
        ANSI_CODES.put("inverse", "7");
        ANSI_CODES.put("hidden", "8");
        ANSI_CODES.put("gray", "30");
        ANSI_CODES.put("red", "31");
        ANSI_CODES.put("green", "32");
        ANSI_CODES.put("yellow", "33");
        ANSI_CODES.put("blue", "34");
        ANSI_CODES.put("magenta", "35");
        ANSI_CODES.put("cyan", "36");
        ANSI_CODES.put("silver", "0;37");
        ANSI_CODES.put("white", "37");
        ANSI_CODES.put("black_bg", "40");
        ANSI_CODES.put("red_bg", "41");
        ANSI_CODES.put("green_bg", "42");
        ANSI_CODES.put("yellow_bg", "43");
        ANSI_CODES.put("blue_bg", "44");
        ANSI_CODES.put("magenta_bg", "45");
        ANSI_CODES.put("cyan_bg", "46");
        ANSI_CODES.put("white_bg", "47");
    }

    public static String withColor(String str, String color, boolean off, boolean fill) {
        String[] colorAttrs = color.split("\\+");
        StringBuilder ansiStr = new StringBuilder();

        for (String attr : colorAttrs) {
            ansiStr.append((char) 27).append("[").append(ANSI_CODES.get(attr)).append("m");
        }

        if (off) {
            ansiStr.append(str).append((char) 27).append("[").append(ANSI_CODES.get("off")).append("m");
        } else {
            ansiStr.append(str).append((char) 27).append("[").append(ANSI_CODES.getOrDefault(off ? "off" : "none", "0")).append("m");
        }

        return ansiStr.toString();
    }

    private static boolean hasColorSupport() {
        return System.console() != null && System.getenv().get("TERM") != null && System.getenv().get("TERM").equals("xterm-256color");
    }

    // ниже код для проверки работоспособности, в релизной версии tulpar-server будет удален этот код ниже.
    public static void main(String[] args) {
        System.out.println(withColor("тестстеттс", "red+bold", true, false));
        System.out.println(withColor("utf-8 with colors ееее", "green+underline", true, false));
    }
}
