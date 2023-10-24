package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Log {
    public static boolean DEBUG_MODE = true;
    public static boolean SILENT = false;

    private static final Logger logger = LogManager.getLogger(Log.class);

    public static void debug(Object... args) {
//        BasicConfigurator.configure();
        if (Log.SILENT) {
            return;
        }
        if (Log.DEBUG_MODE) {
            logger.debug(args[0]);
        }
    }

    public static void log(Object... args) {
//        BasicConfigurator.configure();
        if (Log.SILENT) {
            return;
        }
        logger.info(args[0]);
    }

    public static void warn(Object... args) {
//        BasicConfigurator.configure();
        if (Log.SILENT) {
            return;
        }
        logger.warn(args[0]);
    }
}
