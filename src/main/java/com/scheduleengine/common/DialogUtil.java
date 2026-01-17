package com.scheduleengine.common;

import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Utility class for making dialogs resizable and persisting their size preferences.
 */
public class DialogUtil {

  private static final Preferences prefs = Preferences.userNodeForPackage(DialogUtil.class);
  private static final String WIDTH_SUFFIX = ".width";
  private static final String HEIGHT_SUFFIX = ".height";
  private static final double DEFAULT_WIDTH = 500;
  private static final double DEFAULT_HEIGHT = 400;

  /**
   * Makes a dialog resizable and restores/persists its size from user preferences.
   *
   * @param dialog   The dialog to configure
   * @param dialogId A unique identifier for this dialog (e.g., "league.add", "season.edit")
   */
  public static void makeResizable(Dialog<?> dialog, String dialogId) {
    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();

    // Make dialog resizable
    stage.setResizable(true);

    // Set minimum size
    stage.setMinWidth(400);
    stage.setMinHeight(300);

    // Restore saved size from preferences
    double savedWidth = prefs.getDouble(dialogId + WIDTH_SUFFIX, DEFAULT_WIDTH);
    double savedHeight = prefs.getDouble(dialogId + HEIGHT_SUFFIX, DEFAULT_HEIGHT);

    stage.setWidth(savedWidth);
    stage.setHeight(savedHeight);

    // Save size when dialog closes
    stage.setOnHiding(event -> {
      prefs.putDouble(dialogId + WIDTH_SUFFIX, stage.getWidth());
      prefs.putDouble(dialogId + HEIGHT_SUFFIX, stage.getHeight());
    });
  }

  /**
   * Makes a dialog resizable with custom default dimensions.
   *
   * @param dialog        The dialog to configure
   * @param dialogId      A unique identifier for this dialog
   * @param defaultWidth  Default width if no preference exists
   * @param defaultHeight Default height if no preference exists
   */
  public static void makeResizable(Dialog<?> dialog, String dialogId, double defaultWidth, double defaultHeight) {
    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();

    // Make dialog resizable
    stage.setResizable(true);

    // Set minimum size
    stage.setMinWidth(400);
    stage.setMinHeight(300);

    // Restore saved size from preferences
    double savedWidth = prefs.getDouble(dialogId + WIDTH_SUFFIX, defaultWidth);
    double savedHeight = prefs.getDouble(dialogId + HEIGHT_SUFFIX, defaultHeight);

    stage.setWidth(savedWidth);
    stage.setHeight(savedHeight);

    // Save size when dialog closes
    stage.setOnHiding(event -> {
      prefs.putDouble(dialogId + WIDTH_SUFFIX, stage.getWidth());
      prefs.putDouble(dialogId + HEIGHT_SUFFIX, stage.getHeight());
    });
  }

  /**
   * Reset all saved dialog size preferences.
   */
  public static void resetAllPreferences() {
    try {
      prefs.clear();
    } catch (Exception e) {
      // Silently fail if preferences can't be cleared
    }
  }

  /**
   * Reset preferences for a specific dialog.
   *
   * @param dialogId The dialog identifier
   */
  public static void resetPreferences(String dialogId) {
    prefs.remove(dialogId + WIDTH_SUFFIX);
    prefs.remove(dialogId + HEIGHT_SUFFIX);
  }
}

