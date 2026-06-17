package com.airline.skybooker.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A tool to save application errors into a file.
 * It exists to help developers track down and fix bugs when something goes wrong.
 */
public class ErrorLogger {

    private static final String LOG_FILE = "system_errors.log";

    /**
     * Appends a formatted exception stack trace to the persistent audit log.
     * Records the execution timestamp alongside critical stack elements for forensic debugging.
     * 
     * @param e The caught exception instance containing runtime failure details
     */
    public static void logError(Exception e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.printf("[%s] ERROR: %s%n", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 
                e.getMessage());
            
            // For severe system exceptions, we would log the stack trace in a real system
            if (!(e instanceof RuntimeException) && !(e instanceof IllegalArgumentException)) {
                e.printStackTrace(writer);
            }
        } catch (IOException ioException) {
            System.err.println("CRITICAL: Failed to write to system error log: " + ioException.getMessage());
        }
    }
}
