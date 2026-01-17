package com.scheduleengine.common;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Utility class for persisting and restoring table column widths via user preferences.
 * Automatically saves column widths when they change and restores them on startup.
 */
public class TablePreferencesUtil {

  private static final Preferences prefs = Preferences.userNodeForPackage(TablePreferencesUtil.class);
  private static final String COLUMN_WIDTH_SUFFIX = ".column.width";

  // Track which tables are currently being restored to avoid saving during restore
  private static final Map<String, Boolean> restoringTables = new HashMap<>();

  /**
   * Setup a table to automatically persist and restore column widths.
   * Call this after the table and columns are created but before adding data.
   *
   * @param table   The TableView to configure
   * @param tableId A unique identifier for this table (e.g., "league.table", "team.table")
   */
  public static void setupTableColumnPersistence(TableView<?> table, String tableId) {
    // Listen for column width changes and save them (but not during restore)
    table.getColumns().forEach(column -> {
      column.widthProperty().addListener((obs, oldVal, newVal) -> {
        // Don't save during restore operation
        if (Boolean.TRUE.equals(restoringTables.get(tableId))) {
          return;
        }
        if (newVal != null && newVal.doubleValue() > 0) {
          saveColumnWidth(tableId, getColumnKey(column), newVal.doubleValue());
        }
      });
    });

    // Restore saved column widths after the table is shown (when scene is set)
    table.sceneProperty().addListener((obs, oldScene, newScene) -> {
      if (newScene != null && oldScene == null) {
        // Use runLater to ensure layout is complete
        javafx.application.Platform.runLater(() -> restoreColumnWidths(table, tableId));
      }
    });

    // Also restore when table becomes visible (handles view switches without scene changes)
    table.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
      if (isVisible) {
        javafx.application.Platform.runLater(() -> restoreColumnWidths(table, tableId));
      }
    });

    // Also restore after skin is applied (ensures column header and resize policies are active)
    table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
      if (newSkin != null) {
        javafx.application.Platform.runLater(() -> restoreColumnWidths(table, tableId));
      }
    });
  }

  /**
   * Restore column widths from preferences for a table.
   *
   * @param table   The TableView to restore widths for
   * @param tableId The table identifier
   */
  private static void restoreColumnWidths(TableView<?> table, String tableId) {
    // Set flag to prevent saving during restore
    restoringTables.put(tableId, true);
    try {
      for (int i = 0; i < table.getColumns().size(); i++) {
        TableColumn<?, ?> column = table.getColumns().get(i);
        String columnKey = getColumnKey(column);
        String prefKey = tableId + COLUMN_WIDTH_SUFFIX + "." + columnKey;

        double savedWidth = prefs.getDouble(prefKey, -1);
        if (savedWidth > 0) {
          column.setPrefWidth(savedWidth);
        }
      }
    } finally {
      // Clear flag after restore is complete
      restoringTables.put(tableId, false);
    }
  }

  /**
   * Save a single column width to preferences.
   *
   * @param tableId   The table identifier
   * @param columnKey The column key
   * @param width     The width to save
   */
  private static void saveColumnWidth(String tableId, String columnKey, double width) {
    try {
      String prefKey = tableId + COLUMN_WIDTH_SUFFIX + "." + columnKey;
      prefs.putDouble(prefKey, width);
      prefs.sync();
    } catch (Exception e) {
      System.err.println("Failed to save table column width preference: " + e.getMessage());
    }
  }

  /**
   * Get a unique key for a table column.
   * Uses the column text if available, otherwise uses the id or a hash of the column.
   *
   * @param column The column
   * @return A unique key for the column
   */
  private static String getColumnKey(javafx.scene.control.TableColumn<?, ?> column) {
    if (column.getText() != null && !column.getText().isEmpty()) {
      return column.getText().toLowerCase().replaceAll("\\s+", "_");
    }
    if (column.getId() != null && !column.getId().isEmpty()) {
      return column.getId();
    }
    return "column_" + System.identityHashCode(column);
  }

  /**
   * Clear all saved table preferences.
   */
  public static void resetAllTablePreferences() {
    try {
      String[] keys = prefs.keys();
      for (String key : keys) {
        if (key.contains(COLUMN_WIDTH_SUFFIX)) {
          prefs.remove(key);
        }
      }
      prefs.sync();
    } catch (Exception e) {
      System.err.println("Failed to reset table preferences: " + e.getMessage());
    }
  }

  /**
   * Clear preferences for a specific table.
   *
   * @param tableId The table identifier
   */
  public static void resetTablePreferences(String tableId) {
    try {
      String[] keys = prefs.keys();
      for (String key : keys) {
        if (key.startsWith(tableId) && key.contains(COLUMN_WIDTH_SUFFIX)) {
          prefs.remove(key);
        }
      }
      prefs.sync();
    } catch (Exception e) {
      System.err.println("Failed to reset table preferences: " + e.getMessage());
    }
  }

  public static void bind(javafx.scene.control.TableView<?> table, String key) {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(TablePreferencesUtil.class);
    String base = "table." + key + ".";
    // Restore widths/visibility
    for (javafx.scene.control.TableColumn<?, ?> col : table.getColumns()) {
      String id = ensureId(col);
      double w = prefs.getDouble(base + id + ".width", -1);
      boolean vis = prefs.getBoolean(base + id + ".visible", true);
      if (w > 0) col.setPrefWidth(w);
      col.setVisible(vis);
    }
    // Restore sort
    String sortIds = prefs.get(base + "sort.ids", "");
    String sortTypes = prefs.get(base + "sort.types", "");
    if (!sortIds.isBlank()) {
      String[] ids = sortIds.split(",");
      String[] types = sortTypes.split(",");
      table.getSortOrder().clear();
      for (int i = 0; i < ids.length; i++) {
        javafx.scene.control.TableColumn<?, ?> col = findColumnById(table, ids[i]);
        if (col != null) {
          try {
            javafx.scene.control.TableColumn.SortType st = javafx.scene.control.TableColumn.SortType.valueOf(types[i]);
            col.setSortType(st);
            @SuppressWarnings({"rawtypes", "unchecked"})
            javafx.scene.control.TableColumn raw = col;
            table.getSortOrder().add(raw);
          } catch (Exception ignored) {
          }
        }
      }
      table.sort();
    }
    // Listen and persist
    for (javafx.scene.control.TableColumn<?, ?> col : table.getColumns()) {
      col.widthProperty().addListener((obs, o, n) -> prefs.putDouble(base + ensureId(col) + ".width", n.doubleValue()));
      col.visibleProperty().addListener((obs, o, n) -> prefs.putBoolean(base + ensureId(col) + ".visible", n));
    }
    table.getSortOrder().addListener((javafx.collections.ListChangeListener<javafx.scene.control.TableColumn<?, ?>>) change -> saveSort(table, base, prefs));
  }

  public static void attachToggleMenu(javafx.scene.control.TableView<?> table, String key) {
    javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
    for (javafx.scene.control.TableColumn<?, ?> col : table.getColumns()) {
      javafx.scene.control.CheckMenuItem item = new javafx.scene.control.CheckMenuItem(col.getText());
      item.setSelected(col.isVisible());
      item.selectedProperty().addListener((obs, o, n) -> col.setVisible(n));
      menu.getItems().add(item);
    }
    // Add a global reset option that clears persisted prefs for this table and reapplies defaults
    javafx.scene.control.MenuItem resetItem = new javafx.scene.control.MenuItem("Reset Columns (Clear Saved Preferences)");
    resetItem.setOnAction(e -> {
      // Clear persisted widths/visibility/sort for this table key
      String base = "table." + key + ".";
      java.util.prefs.Preferences p = java.util.prefs.Preferences.userNodeForPackage(TablePreferencesUtil.class);
      try {
        String[] keys = p.keys();
        for (String k : keys) {
          if (k.startsWith(base)) {
            p.remove(k);
          }
        }
        p.sync();
      } catch (Exception ignored) {
      }
      // Reset UI to defaults: make all visible and clear sort; widths revert to current prefWidth
      for (javafx.scene.control.TableColumn<?, ?> col : table.getColumns()) {
        col.setVisible(true);
      }
      table.getSortOrder().clear();
      table.sort();
    });
    menu.getItems().add(new javafx.scene.control.SeparatorMenuItem());
    menu.getItems().add(resetItem);
    table.setContextMenu(menu);
  }

  private static javafx.scene.control.TableColumn<?, ?> findColumnById(javafx.scene.control.TableView<?> table, String id) {
    for (javafx.scene.control.TableColumn<?, ?> c : table.getColumns()) {
      if (id.equals(c.getId())) return c;
    }
    return null;
  }

  private static void saveSort(javafx.scene.control.TableView<?> table, String base, java.util.prefs.Preferences prefs) {
    java.util.List<String> ids = new java.util.ArrayList<>();
    java.util.List<String> types = new java.util.ArrayList<>();
    for (javafx.scene.control.TableColumn<?, ?> c : table.getSortOrder()) {
      ids.add(ensureId(c));
      types.add(c.getSortType().name());
    }
    prefs.put(base + "sort.ids", String.join(",", ids));
    prefs.put(base + "sort.types", String.join(",", types));
  }

  private static String ensureId(javafx.scene.control.TableColumn<?, ?> col) {
    if (col.getId() == null || col.getId().isBlank()) {
      String id = col.getText() == null ? "col" : col.getText().toLowerCase().replaceAll("[^a-z0-9]+", "_");
      col.setId(id);
    }
    return col.getId();
  }
}
