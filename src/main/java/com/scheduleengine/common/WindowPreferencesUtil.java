package com.scheduleengine.common;

import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Utility class for persisting and restoring main application window state.
 * Stores and restores: window width, height, X position, Y position, and maximized state.
 */
public class WindowPreferencesUtil {

  private static final Preferences prefs = Preferences.userNodeForPackage(WindowPreferencesUtil.class);

  // Preference keys
  private static final String WINDOW_WIDTH = "window.width";
  private static final String WINDOW_HEIGHT = "window.height";
  private static final String WINDOW_X = "window.x";
  private static final String WINDOW_Y = "window.y";
  private static final String WINDOW_MAXIMIZED = "window.maximized";

  // Default window dimensions
  private static final double DEFAULT_WIDTH = 1400;
  private static final double DEFAULT_HEIGHT = 900;
  private static final double DEFAULT_X = 100;
  private static final double DEFAULT_Y = 100;

  /**
   * Restore the main window state from saved preferences.
   * If no preferences exist, uses sensible defaults.
   *
   * @param stage The main application stage
   */
  public static void restoreWindowState(Stage stage) {
    // Restore window dimensions
    double width = prefs.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH);
    double height = prefs.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT);
    double x = prefs.getDouble(WINDOW_X, DEFAULT_X);
    double y = prefs.getDouble(WINDOW_Y, DEFAULT_Y);
    boolean maximized = prefs.getBoolean(WINDOW_MAXIMIZED, false);

    // Validate dimensions (in case screen resolution changed)
    width = Math.max(800, Math.min(width, 2560));  // Min 800, max 2560
    height = Math.max(600, Math.min(height, 1440)); // Min 600, max 1440

    // Validate position (in case monitor layout changed)
    x = Math.max(0, Math.min(x, 1920));
    y = Math.max(0, Math.min(y, 1080));

    // Apply restored state
    stage.setWidth(width);
    stage.setHeight(height);
    stage.setX(x);
    stage.setY(y);
    stage.setMaximized(maximized);
  }

  /**
   * Setup the main window to save its state on close.
   * Should be called after the window is shown.
   *
   * @param stage The main application stage
   */
  public static void setupWindowStatePersistence(Stage stage) {
    // Save window state when the application closes
    stage.setOnCloseRequest(event -> saveWindowState(stage));

    // Also save state periodically (e.g., every 10 seconds) in case of unexpected shutdown
    setupPeriodicSave(stage);
  }

  /**
   * Save the current window state to preferences.
   *
   * @param stage The main application stage
   */
  public static void saveWindowState(Stage stage) {
    // Don't save if window is minimized
    if (stage.isIconified()) {
      return;
    }

    try {
      // Save dimensions and position
      prefs.putDouble(WINDOW_WIDTH, stage.getWidth());
      prefs.putDouble(WINDOW_HEIGHT, stage.getHeight());
      prefs.putDouble(WINDOW_X, stage.getX());
      prefs.putDouble(WINDOW_Y, stage.getY());
      prefs.putBoolean(WINDOW_MAXIMIZED, stage.isMaximized());

      // Force synchronization to disk
      prefs.sync();
    } catch (Exception e) {
      System.err.println("Failed to save window preferences: " + e.getMessage());
    }
  }

  /**
   * Setup periodic saving of window state (every 10 seconds)
   * Provides protection against unexpected application crashes
   *
   * @param stage The main application stage
   */
  private static void setupPeriodicSave(Stage stage) {
    Thread saveThread = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(10000); // Save every 10 seconds
          saveWindowState(stage);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        } catch (Exception e) {
          // Ignore errors in periodic save
        }
      }
    });

    // Set as daemon thread so it doesn't prevent app shutdown
    saveThread.setDaemon(true);
    saveThread.setName("WindowPreferenceSaveThread");
    saveThread.start();
  }

  /**
   * Reset all window preferences to defaults.
   * Window will use default size/position on next launch.
   */
  public static void resetWindowPreferences() {
    try {
      prefs.remove(WINDOW_WIDTH);
      prefs.remove(WINDOW_HEIGHT);
      prefs.remove(WINDOW_X);
      prefs.remove(WINDOW_Y);
      prefs.remove(WINDOW_MAXIMIZED);
      prefs.sync();
    } catch (Exception e) {
      System.err.println("Failed to reset window preferences: " + e.getMessage());
    }
  }

  /**
   * Check if window preferences exist
   *
   * @return true if window preferences have been saved before
   */
  public static boolean hasPreferences() {
    return prefs.get(WINDOW_WIDTH, null) != null;
  }
}

