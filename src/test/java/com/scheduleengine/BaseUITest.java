package com.scheduleengine;

import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for UI automation tests with screenshot support.
 * Provides utilities for capturing screenshots at each test step.
 */
public abstract class BaseUITest extends ApplicationTest {
    
    protected static Path screenshotsDir;
    protected String currentTestName;
    protected int screenshotCounter = 0;
    
    @BeforeAll
    public static void setupHeadless() throws Exception {
        // Set up headless mode for CI/CD environments
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
        
        // Set up screenshots directory
        String screenshotsDirPath = System.getProperty("screenshots.dir", "target/screenshots");
        screenshotsDir = Paths.get(screenshotsDirPath);
        Files.createDirectories(screenshotsDir);
    }
    
    @AfterEach
    public void cleanup(TestInfo testInfo) throws Exception {
        currentTestName = null;
        screenshotCounter = 0;
        FxToolkit.cleanupStages();
    }
    
    /**
     * Captures a screenshot of the current stage with a descriptive name.
     * Screenshots are saved to target/screenshots with timestamp and step number.
     * 
     * @param stepDescription Description of what this screenshot shows
     */
    protected void captureScreenshot(String stepDescription) {
        try {
            screenshotCounter++;
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%02d_%s_%s.png", 
                getCurrentTestClassName(),
                screenshotCounter,
                timestamp,
                sanitizeFileName(stepDescription));
            
            Path screenshotPath = screenshotsDir.resolve(fileName);
            
            // Capture the screenshot
            WritableImage snapshot = captureWindow();
            
            if (snapshot != null) {
                saveImage(snapshot, screenshotPath.toFile());
                System.out.println("Screenshot saved: " + screenshotPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Captures the entire window
     */
    private WritableImage captureWindow() {
        try {
            // Get the primary stage
            Stage stage = listTargetWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .findFirst()
                .orElse(null);
            
            if (stage != null && stage.getScene() != null) {
                return stage.getScene().snapshot(null);
            }
        } catch (Exception e) {
            System.err.println("Error capturing window: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Saves JavaFX WritableImage to a PNG file
     */
    private void saveImage(WritableImage image, File file) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(
            (int) image.getWidth(),
            (int) image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int argb = image.getPixelReader().getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }
        
        ImageIO.write(bufferedImage, "png", file);
    }
    
    /**
     * Gets the current test class name
     */
    private String getCurrentTestClassName() {
        if (currentTestName != null) {
            return currentTestName;
        }
        return getClass().getSimpleName();
    }
    
    /**
     * Sanitizes file name by removing invalid characters
     */
    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();
    }
    
    /**
     * Helper method to wait for JavaFX thread operations
     */
    protected void waitForFxEvents() {
        sleep(200); // Small delay to allow JavaFX to process events
    }
}
