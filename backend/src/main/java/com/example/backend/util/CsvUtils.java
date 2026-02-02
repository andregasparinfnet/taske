package com.example.backend.util;

/**
 * Utility class for CSV operations with security focus.
 * Implements defenses against CSV Injection (OWASP).
 */
public class CsvUtils {

    /**
     * Sanitizes a string for CSV export.
     * 1. Handles nulls.
     * 2. Escapes double quotes by doubling them.
     * 3. Wraps in double quotes if it contains delimiters (comma, newline, or quotes).
     * 4. Prevents CSV Injection by prepending a single quote to dangerous starters (=, +, -, @).
     *
     * @param s The string to sanitize
     * @return Sanitized string
     */
    public static String sanitize(String s) {
        if (s == null) {
            return "";
        }

        // SEC: Prevent CSV Injection (DDE attacks)
        // If the cell starts with a formula trigger, prepend a single quote
        String value = s;
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            value = "'" + value;
        }

        // Standard CSV Escaping
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        
        return escaped;
    }
}
