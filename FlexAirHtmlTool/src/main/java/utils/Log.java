package utils;

public class Log {
    public static boolean DEBUG_MODE = true;
    public static boolean SILENT = false;

    public static void debug(Object... args) {
        if (Log.SILENT) {
            return;
        }
        if (Log.DEBUG_MODE) {
            System.out.println(args[0]);
        }
    }

    public static void log(Object... args) {
        if (Log.SILENT) {
            return;
        }
        System.out.println(args[0]);
    }

    public static void warn(Object... args) {
        if (Log.SILENT) {
            return;
        }
        System.out.println(args[0]);
    }
}
