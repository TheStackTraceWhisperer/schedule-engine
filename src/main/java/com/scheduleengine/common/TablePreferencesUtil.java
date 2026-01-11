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
     * @param table The TableView to configure
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
     * @param table The TableView to restore widths for
     * @param tableId The table identifier
     */
    private static void restoreColumnWidths(TableView<?> table, String tableId) {
        // Set flag to prevent saving during restore
        restoringTables.put(tableId, true);
        try {
            for (int i = 0; i < table.getColumns().size(); i++) {
                TableColumn<?,?> column = table.getColumns().get(i);
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
     * @param tableId The table identifier
     * @param columnKey The column key
     * @param width The width to save
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
}
