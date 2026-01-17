package com.scheduleengine.common;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * UI Scale Control Panel for easy access to zoom/scale settings.
 * Now uses a dropdown with fixed options (Small, Default, Large, Extra Large).
 * Applies scale changes in real-time to the provided Scene.
 */
public class UIScaleControlPanel extends VBox {

  private Label scaleLabel;
  private final Scene scene;
  private Runnable onScaleChangeCallback;
  private ComboBox<String> scaleDropdown;

  public UIScaleControlPanel(Scene scene) {
    this.scene = scene;

    setSpacing(10);
    setPadding(new Insets(12, 16, 12, 16));
    setStyle("-fx-background-color: #34495e; -fx-border-color: #2c3e50; -fx-border-width: 1 0 0 0;");

    // Title
    Label titleLabel = new Label("UI Scale");
    titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

    // Scale display and controls
    HBox controlBox = createControlBox();

    getChildren().addAll(titleLabel, controlBox);
  }

  private HBox createControlBox() {
    HBox box = new HBox(8);
    box.setAlignment(Pos.CENTER_LEFT);
    box.setStyle("-fx-background-color: transparent;");

    Label label = new Label("Scale:");
    label.setStyle("-fx-font-size: 12px; -fx-text-fill: #ecf0f1;");

    // Dropdown with minimal options
    scaleDropdown = new ComboBox<>(FXCollections.observableArrayList(
      "75%", "100%", "125%", "150%"
    ));

    scaleDropdown.setStyle(
      "-fx-font-size: 12px; " +
        "-fx-background-color: #2c3e50; " +
        "-fx-text-fill: #ecf0f1; " +
        "-fx-padding: 4 8; " +
        "-fx-min-width: 100;"
    );

    // Force white text for the button cell and dropdown items
    scaleDropdown.setButtonCell(new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        setTextFill(javafx.scene.paint.Color.web("#ecf0f1"));
        setStyle("-fx-background-color: #2c3e50;");
      }
    });
    scaleDropdown.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
      @Override protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        setTextFill(javafx.scene.paint.Color.web("#ecf0f1"));
        setStyle("-fx-background-color: #2c3e50;");
      }
    });

    // Initialize selected item from current scale
    double current = UIScaleUtil.getScale();
    String currentPct = String.format("%.0f%%", current * 100);
    if (!scaleDropdown.getItems().contains(currentPct)) {
      currentPct = "100%";
    }
    scaleDropdown.getSelectionModel().select(currentPct);

    // Apply on change
    scaleDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal == null) return;
      double newScale;
      switch (newVal) {
        case "75%":
          newScale = 0.75;
          break;
        case "100%":
          newScale = 1.0;
          break;
        case "125%":
          newScale = 1.25;
          break;
        case "150%":
          newScale = 1.50;
          break;
        default:
          newScale = 1.0;
          break;
      }
      UIScaleUtil.setScale(newScale);
      applyScaleToScene();
      notifyScaleChange();
    });

    box.getChildren().addAll(label, scaleDropdown);
    return box;
  }

  /**
   * Apply the current scale to the scene by updating CSS
   */
  private void applyScaleToScene() {
    if (scene == null) {
      return;
    }

    double scale = UIScaleUtil.getScale();

    // Create a global stylesheet that scales fonts and spacing
    String globalCSS = String.format(
      "* { -fx-font-size: %.0fpx; } " +
        ".root { -fx-font-size: %.0fpx; } " +
        ".label { -fx-font-size: %.0fpx; } " +
        ".button { -fx-font-size: %.0fpx; -fx-padding: %.1fem; } " +
        ".text-field { -fx-font-size: %.0fpx; -fx-padding: %.1fem; } " +
        ".combo-box { -fx-font-size: %.0fpx; } " +
        ".table-view { -fx-font-size: %.0fpx; } " +
        ".table-view .table-cell { -fx-padding: %.1fem; }",
      12.0 * scale,
      12.0 * scale,
      13.0 * scale,
      13.0 * scale,
      0.5 * scale,
      12.0 * scale,
      0.5 * scale,
      12.0 * scale,
      12.0 * scale,
      0.2 * scale
    );

    // Clear existing user agent stylesheets and add new one
    scene.getStylesheets().clear();
    scene.getStylesheets().add("data:text/css," + globalCSS);
  }

  /**
   * Set a callback to be called when scale changes
   */
  public void setOnScaleChange(Runnable callback) {
    this.onScaleChangeCallback = callback;
  }

  /**
   * Notify that scale has changed
   */
  private void notifyScaleChange() {
    if (onScaleChangeCallback != null) {
      onScaleChangeCallback.run();
    }
  }
}
