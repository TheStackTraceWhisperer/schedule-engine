package com.scheduleengine.common;

import javafx.util.StringConverter;

import java.util.Comparator;

/**
 * Utilities for JavaFX TableColumn numeric handling to ensure consistent numeric sorting and editing.
 */
public final class TableColumnUtil {
  private TableColumnUtil() {
  }

  // Numeric comparators
  public static Comparator<Number> comparingInt() {
    return Comparator.comparingInt(n -> n == null ? 0 : n.intValue());
  }

  public static Comparator<Number> comparingLong() {
    return Comparator.comparingLong(n -> n == null ? 0L : n.longValue());
  }

  public static Comparator<Number> comparingDouble() {
    return Comparator.comparingDouble(n -> n == null ? 0.0 : n.doubleValue());
  }

  // StringConverters for editable numeric cells
  public static StringConverter<Number> integerStringConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(Number object) {
        return object == null ? "" : String.valueOf(object.intValue());
      }

      @Override
      public Number fromString(String string) {
        if (string == null || string.isBlank()) return null;
        try {
          return Integer.parseInt(string.trim());
        } catch (Exception e) {
          return null;
        }
      }
    };
  }

  public static StringConverter<Number> doubleStringConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(Number object) {
        return object == null ? "" : String.valueOf(object.doubleValue());
      }

      @Override
      public Number fromString(String string) {
        if (string == null || string.isBlank()) return null;
        try {
          return Double.parseDouble(string.trim());
        } catch (Exception e) {
          return null;
        }
      }
    };
  }

  // Helper to parse leading integer from "count/max" text (e.g., "12/32")
  public static int parseLeadingInt(String s) {
    if (s == null) return 0;
    int slash = s.indexOf('/');
    String lead = slash >= 0 ? s.substring(0, slash) : s;
    try {
      return Integer.parseInt(lead.trim());
    } catch (Exception e) {
      return 0;
    }
  }
}
