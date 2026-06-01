package com.airline.skybooker.utils;

import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Utility class for reading validated console input to maintain DRY principles.
 */
public class InputReader {

    /**
     * Reads a required string and validates it against a provided validator.
     * Keeps looping until the input passes validation.
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
     * Reads an optional string. If the user presses enter (empty input), it returns empty string.
     * Otherwise it validates against the provided validator.
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
     * Reads an integer with an optional validation step.
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
     * Reads a double with an optional validation step.
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
