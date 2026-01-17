package com.scheduleengine.navigation;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A card component for drill-down navigation options
 */
public class DrillDownCard extends VBox {

  private final Runnable onAction;

  public DrillDownCard(String title, String description, FontAwesomeIcon icon, Runnable onAction) {
    this.onAction = onAction;

    setPadding(new Insets(20));
    setSpacing(10);
    setStyle(
      "-fx-background-color: white; " +
        "-fx-border-color: #e0e0e0; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 8; " +
        "-fx-background-radius: 8; " +
        "-fx-cursor: hand; " +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);"
    );

    // Header with icon and title
    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);

    FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
    iconView.setSize("24");
    iconView.setFill(javafx.scene.paint.Color.web("#667eea"));

    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    FontAwesomeIconView arrowIcon = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_RIGHT);
    arrowIcon.setSize("14");
    arrowIcon.setFill(javafx.scene.paint.Color.web("#95a5a6"));

    header.getChildren().addAll(iconView, titleLabel, spacer, arrowIcon);

    // Description
    Label descLabel = new Label(description);
    descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
    descLabel.setWrapText(true);

    getChildren().addAll(header, descLabel);

    // Hover effects
    setOnMouseEntered(e -> setStyle(
      "-fx-background-color: #f8f9ff; " +
        "-fx-border-color: #667eea; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 8; " +
        "-fx-background-radius: 8; " +
        "-fx-cursor: hand; " +
        "-fx-effect: dropshadow(three-pass-box, rgba(102, 126, 234, 0.3), 8, 0, 0, 2);"
    ));

    setOnMouseExited(e -> setStyle(
      "-fx-background-color: white; " +
        "-fx-border-color: #e0e0e0; " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 8; " +
        "-fx-background-radius: 8; " +
        "-fx-cursor: hand; " +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);"
    ));

    setOnMouseClicked(e -> {
      if (onAction != null) {
        onAction.run();
      }
    });

    // Minimum height
    setMinHeight(100);
    setPrefHeight(100);
  }
}

