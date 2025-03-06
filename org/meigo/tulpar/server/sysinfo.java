package org.meigo.tulpar.server;

import java.io.UnsupportedEncodingException;

public class sysinfo {
    public static String arch;
    public static String os;
    public static String osver;

    public static void init() throws UnsupportedEncodingException {
        Logger.devinfo("Initializing system information...");
        arch = System.getProperty("os.arch");
        osver = System.getProperty("os.version");

        // Determine OS family (Linux/Windows/macOS)
        // по идеи, есть и другие системы по типу FreeBSD или че та там ещё
        // для них нужно зависимости компилировать, поэтому я просто сделаю гайд
        // в котором будет расписано как под любую НН ос сделать поддержку Tulpar Server
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            os = "Windows";
            Main.enableColorsForWindows();
            Logger.warn("Since you are using Windows, we have included native support for ANSI colours.");
            Main.utf8enable();
            Logger.warn("UTF8 colour native support was enabled");
            Main.setwindowtitle();
            Logger.warn("Title was changed");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            os = "Linux";
        } else if (osName.contains("mac")) {
            os = "macOS";
            Logger.error("Server doesn't support MacOS X");
            System.exit(1);
        } else {
            String oldos = System.getProperty("os.name");
            Logger.warn("Unknown operating system! Installing Linux. Old OS: " + oldos);
            os = "Linux";
        }

        Logger.devinfo("System Info set: arch=" + arch + ", os=" + os + ", osver=" + osver);
    }
}
