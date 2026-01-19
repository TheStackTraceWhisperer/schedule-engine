package com.scheduleengine.common;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class IconBadge {
  public static StackPane build(String iconName, String bgColor, String glyphColor, double size) {
    String bg = bgColor != null ? bgColor : "#2c3e50";
    String fg = glyphColor != null ? glyphColor : "#ffffff";
    String icon = iconName != null ? iconName : FontAwesomeIcon.USERS.name();
    FontAwesomeIcon fa = FontAwesomeIcon.valueOf(icon);
    FontAwesomeIconView iconView = new FontAwesomeIconView(fa);
    iconView.setGlyphSize(size / 2);
    iconView.setFill(Color.web(fg));

    StackPane badge = new StackPane(iconView);
    badge.setPrefSize(size, size);
    badge.setMinSize(size, size);
    badge.setMaxSize(size, size);
    badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: " + (size/2) + "px;");
    return badge;
  }
}
