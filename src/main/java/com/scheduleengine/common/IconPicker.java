package com.scheduleengine.common;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class IconPicker extends VBox {
  private final TextField iconSearch = new TextField();
  private final TilePane iconGrid = new TilePane();
  private final ScrollPane iconScroll = new ScrollPane(iconGrid);
  private final ColorPicker bgPicker;
  private final ColorPicker glyphPicker;
  private final FontAwesomeIconView previewIcon;
  private final VBox pickerBox;
  private final HBox previewBox;

  private String selectedIcon;

  public static class Selection {
    public final String iconName;
    public final String bgColor;
    public final String glyphColor;

    public Selection(String iconName, String bgColor, String glyphColor) {
      this.iconName = iconName;
      this.bgColor = bgColor;
      this.glyphColor = glyphColor;
    }
  }

  public IconPicker(String initialIcon, String initialBg, String initialGlyph, Consumer<Selection> onChange) {
    setSpacing(8);
    setPadding(new Insets(0));
    iconSearch.setPromptText("Search icons (e.g., USERS, TROPHY)...");

    iconGrid.setHgap(10);
    iconGrid.setVgap(10);
    iconGrid.setPrefColumns(8);

    iconScroll.setFitToWidth(true);
    iconScroll.setPrefViewportHeight(220);
    iconScroll.setStyle("-fx-background-color: transparent;");

    selectedIcon = initialIcon != null ? initialIcon : FontAwesomeIcon.USERS.name();

    previewIcon = new FontAwesomeIconView(FontAwesomeIcon.valueOf(selectedIcon));
    previewIcon.setGlyphSize(48);

    bgPicker = new ColorPicker(Color.web(initialBg != null ? initialBg : "#2c3e50"));
    glyphPicker = new ColorPicker(Color.web(initialGlyph != null ? initialGlyph : "#ffffff"));

    Node previewBadge = IconBadge.build(selectedIcon, toHex(bgPicker.getValue()), toHex(glyphPicker.getValue()), 96);
    previewBox = new HBox(10, new Label("Preview:"), previewBadge);

    pickerBox = new VBox(8, iconSearch, iconScroll);

    getChildren().addAll(pickerBox, new Label("Icon Background:"), bgPicker, new Label("Icon Glyph Color:"), glyphPicker, previewBox);

    rebuildGrid();

    iconSearch.textProperty().addListener((obs, o, n) -> rebuildGrid());
    bgPicker.valueProperty().addListener((obs, o, n) -> {
      updatePreview();
      if (onChange != null) onChange.accept(currentSelection());
    });
    glyphPicker.valueProperty().addListener((obs, o, n) -> {
      updatePreview();
      if (onChange != null) onChange.accept(currentSelection());
    });
  }

  private void rebuildGrid() {
    String q = iconSearch.getText() == null ? "" : iconSearch.getText().trim().toUpperCase();
    iconGrid.getChildren().clear();
    for (FontAwesomeIcon icon : FontAwesomeIcon.values()) {
      if (!q.isEmpty() && !icon.name().contains(q)) continue;
      FontAwesomeIconView iv = new FontAwesomeIconView(icon);
      iv.setGlyphSize(36);
      VBox cell = new VBox(iv);
      cell.setPadding(new Insets(16));
      cell.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 8px; -fx-border-color: #ccc; -fx-border-radius: 8px;");
      if (icon.name().equals(selectedIcon)) {
        cell.setStyle(cell.getStyle() + " -fx-border-color: #667eea;");
      }
      cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
        selectedIcon = icon.name();
        previewIcon.setIcon(icon);
        iconGrid.getChildren().forEach(n -> n.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 8px; -fx-border-color: #ccc; -fx-border-radius: 8px;"));
        cell.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 8px; -fx-border-color: #667eea; -fx-border-radius: 8px;");
        updatePreview();
      });
      iconGrid.getChildren().add(cell);
    }
  }

  private void updatePreview() {
    previewIcon.setFill(glyphPicker.getValue());
    previewBox.getChildren().set(1, IconBadge.build(selectedIcon, toHex(bgPicker.getValue()), toHex(glyphPicker.getValue()), 96));
  }

  public Selection currentSelection() {
    return new Selection(selectedIcon, toHex(bgPicker.getValue()), toHex(glyphPicker.getValue()));
  }

  public static String toHex(Color c) {
    return String.format("#%02x%02x%02x",
      (int) Math.round(c.getRed() * 255),
      (int) Math.round(c.getGreen() * 255),
      (int) Math.round(c.getBlue() * 255));
  }
}
