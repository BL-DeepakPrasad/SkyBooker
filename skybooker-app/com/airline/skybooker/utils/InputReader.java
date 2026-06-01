package com.airline.skybooker.utils;

import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Command-line input adapter ensuring standardized and resilient user data extraction.
 * Employs functional validation boundaries to eliminate malformed input propagation.
 */
public class InputReader {

    /**
     * Halts thread execution until a strictly validated character sequence is acquired from standard input.
     * 
     * @param scanner   The active input stream scanner
     * @param prompt    The instructional text presented to the client
     * @param validator The functional condition the input must satisfy
     * @return A sanitized and successfully validated string input
     */
    public static String readString(Scanner scanner, String prompt, Consumer<String> validator) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                if (validator != null) {
                    validator.accept(input);
                }
                return input;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: Invalid input.");
            }
        }
    }

    /**
     * Facilitates optional character sequence input, yielding an empty string on immediate carriage return.
     * 
     * @param scanner   The active input stream scanner
     * @param prompt    The instructional text presented to the client
     * @param validator The functional condition applied if input is non-empty
     * @return The sanitized string input, or an empty string if bypassed
     */
    public static String readOptionalString(Scanner scanner, String prompt, Consumer<String> validator) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return input;
            }
            try {
                if (validator != null) {
                    validator.accept(input);
                }
                return input;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: Invalid input.");
            }
        }
    }

    /**
     * Captures and converts standard input into a 32-bit integer, guaranteeing numeric integrity.
     * 
     * @param scanner   The active input stream scanner
     * @param prompt    The instructional text presented to the client
     * @param validator The functional condition the numeric value must satisfy
     * @return A safely parsed and validated integer
     */
    public static int readInt(Scanner scanner, String prompt, Consumer<Integer> validator) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (validator != null) {
                    validator.accept(value);
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: Invalid input.");
            }
        }
    }

    /**
     * Captures and converts standard input into a double-precision floating point value.
     * 
     * @param scanner   The active input stream scanner
     * @param prompt    The instructional text presented to the client
     * @param validator The functional condition the decimal value must satisfy
     * @return A safely parsed and validated double
     */
    public static double readDouble(Scanner scanner, String prompt, Consumer<Double> validator) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (validator != null) {
                    validator.accept(value);
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid decimal number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: Invalid input.");
            }
        }
    }
}
