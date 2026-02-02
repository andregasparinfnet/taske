package com.example.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilsTest {

    @Test
    @DisplayName("Should return empty string for null input")
    void sanitize_Null() {
        assertEquals("", CsvUtils.sanitize(null));
    }

    @ParameterizedTest
    @CsvSource({
        "Normal Text, Normal Text",
        "Simple, Simple",
        "'', ''"
    })
    @DisplayName("Should not modify safe text")
    void sanitize_SafeText(String input, String expected) {
        assertEquals(expected, CsvUtils.sanitize(input));
    }

    @Test
    @DisplayName("Should wrap in quotes if contains comma")
    void sanitize_WithComma() {
        assertEquals("\"text,with,comma\"", CsvUtils.sanitize("text,with,comma"));
    }

    @Test
    @DisplayName("Should double quotes and wrap if contains quotes")
    void sanitize_WithQuotes() {
        assertEquals("\"text with \"\"quotes\"\"\"", CsvUtils.sanitize("text with \"quotes\""));
    }

    @Test
    @DisplayName("Should wrap in quotes if contains newlines")
    void sanitize_WithNewlines() {
        assertEquals("\"text with\nnewline\"", CsvUtils.sanitize("text with\nnewline"));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("injectionPayloads")
    @DisplayName("Should prevent CSV Injection by prepending single quote")
    void sanitize_CsvInjection(String input, String expected) {
        assertEquals(expected, CsvUtils.sanitize(input));
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> injectionPayloads() {
        return java.util.stream.Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("=1+1", "'=1+1"),
            org.junit.jupiter.params.provider.Arguments.of("+SUM(A1:B1)", "'+SUM(A1:B1)"),
            org.junit.jupiter.params.provider.Arguments.of("-5", "'-5"),
            org.junit.jupiter.params.provider.Arguments.of("@Version", "'@Version")
        );
    }
}
