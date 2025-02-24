package me.nethuli.ticketingsystem.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to log messages
 */
public class LoggingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingHelper.class);

    public static void info(String message) {
        LOGGER.info("[INFO] {}", message);
    }

    public static void warn(String message) {
        LOGGER.warn("[WARN] {}", message);
    }

    public static void error(String message) {
        LOGGER.error("[ERROR] {}", message);
    }

    public static void debug(String message) {
        LOGGER.debug("[DEBUG] {}", message);
    }
}
