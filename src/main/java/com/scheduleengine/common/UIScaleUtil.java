package com.scheduleengine.common;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

/**
 * Utility class for managing UI scale/zoom level across the application.
 * Allows users to easily adjust font sizes and component scaling for accessibility.
 * Scale is persisted via user preferences.
 */
public class UIScaleUtil {

    private static final Preferences prefs = Preferences.userNodeForPackage(UIScaleUtil.class);
    private static final String UI_SCALE_KEY = "ui.scale";

    // Scale levels: 0.85 = 85% (small), 1.0 = 100% (normal), 1.25 = 125% (large), etc.
    private static double currentScale = getDefaultScale();

    // Default scale value
    private static final double DEFAULT_SCALE = 1.0;

    // Scale range
    private static final double MIN_SCALE = 0.75;  // 75% - very small
    private static final double MAX_SCALE = 2.0;   // 200% - very large
    private static final double SCALE_STEP = 0.05; // 5% increment steps

    static {
        // Load saved scale on class initialization
        loadSavedScale();
    }

    /**
     * Load the saved UI scale from preferences
     */
    private static void loadSavedScale() {
        currentScale = prefs.getDouble(UI_SCALE_KEY, DEFAULT_SCALE);
        // Validate loaded scale
        if (currentScale < MIN_SCALE || currentScale > MAX_SCALE) {
            currentScale = DEFAULT_SCALE;
            saveScale();
        }
    }

    /**
     * Get the current UI scale multiplier
     * Example: 1.0 = 100%, 1.25 = 125%, 0.85 = 85%
     */
    public static double getScale() {
        return currentScale;
    }

    /**
     * Set the UI scale and persist to preferences
     *
     * @param scale The scale multiplier (1.0 = 100%)
     */
    public static void setScale(double scale) {
        // Validate scale
        if (scale < MIN_SCALE || scale > MAX_SCALE) {
            throw new IllegalArgumentException(
                String.format("Scale must be between %.2f and %.2f", MIN_SCALE, MAX_SCALE)
            );
        }

        currentScale = scale;
        saveScale();
    }

    /**
     * Increase UI scale by one step (5%)
     * Returns true if scale was increased, false if at maximum
     */
    public static boolean increaseScale() {
        if (currentScale >= MAX_SCALE) {
            return false;
        }

        double newScale = Math.min(currentScale + SCALE_STEP, MAX_SCALE);
        setScale(newScale);
        return true;
    }

    /**
     * Decrease UI scale by one step (5%)
     * Returns true if scale was decreased, false if at minimum
     */
    public static boolean decreaseScale() {
        if (currentScale <= MIN_SCALE) {
            return false;
        }

        double newScale = Math.max(currentScale - SCALE_STEP, MIN_SCALE);
        setScale(newScale);
        return true;
    }

    /**
     * Reset UI scale to default (100%)
     */
    public static void resetToDefault() {
        setScale(DEFAULT_SCALE);
    }

    /**
     * Check if UI scale is at default
     */
    public static boolean isAtDefault() {
        return Math.abs(currentScale - DEFAULT_SCALE) < 0.01; // Allow small float rounding
    }

    /**
     * Get minimum scale value
     */
    public static double getMinScale() {
        return MIN_SCALE;
    }

    /**
     * Get maximum scale value
     */
    public static double getMaxScale() {
        return MAX_SCALE;
    }

    /**
     * Get scale step size
     */
    public static double getScaleStep() {
        return SCALE_STEP;
    }

    /**
     * Get default scale value
     */
    public static double getDefaultScale() {
        return DEFAULT_SCALE;
    }

    /**
     * Apply UI scale to a scene using CSS
     * This scales fonts, spacing, and other UI elements
     *
     * @param scene The JavaFX scene to apply scaling to
     */
    public static void applyScaleToScene(Scene scene) {
        String scaledFontSize = String.format(
            "-fx-font-size: %.1fem;",
            currentScale
        );

        // Create a stylesheet that overrides font sizes
        String stylesheet = String.format(
            "* { -fx-font-size: %.0fpx; }",
            12 * currentScale  // Base font size is 12px
        );

        scene.getStylesheets().clear();
        scene.getStylesheets().add(
            "data:text/css," + stylesheet
        );
    }

    /**
     * Get scale as a percentage string (e.g., "100%", "125%")
     */
    public static String getScaleAsPercentage() {
        return String.format("%.0f%%", currentScale * 100);
    }

    /**
     * Private method to save scale to preferences
     */
    private static void saveScale() {
        try {
            prefs.putDouble(UI_SCALE_KEY, currentScale);
            prefs.sync();
        } catch (Exception e) {
            System.err.println("Failed to save UI scale preferences: " + e.getMessage());
        }
    }
}

