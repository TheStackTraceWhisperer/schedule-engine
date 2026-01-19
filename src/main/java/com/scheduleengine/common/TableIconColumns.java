package com.scheduleengine.common;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * Shared factory for table icon columns that render an IconBadge for rows
 * providing icon name, background color, and glyph color.
 */
public final class TableIconColumns {
  private TableIconColumns() {}

  /**
   * Create a Void-typed TableColumn that renders an IconBadge using the provided accessors.
   *
   * @param <T> row type
   * @param title column title
   * @param iconName accessor for icon name
   * @param bgColor accessor for background color
   * @param glyphColor accessor for glyph color
   * @param size badge dimension in px (square)
   * @param prefWidth column preferred width
   * @return TableColumn<T, Void>
   */
  public static <T> TableColumn<T, Void> iconColumn(
    String title,
    Function<T, String> iconName,
    Function<T, String> bgColor,
    Function<T, String> glyphColor,
    double size,
    double prefWidth
  ) {
    TableColumn<T, Void> col = new TableColumn<>(title);
    col.setPrefWidth(prefWidth);
    col.setCellFactory(new Callback<>() {
      @Override
      public TableCell<T, Void> call(TableColumn<T, Void> param) {
        return new TableCell<>() {
          @Override
          protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) { setGraphic(null); return; }
            T row = getTableView().getItems().get(getIndex());
            StackPane badge = IconBadge.build(iconName.apply(row), bgColor.apply(row), glyphColor.apply(row), size);
            setGraphic(badge);
          }
        };
      }
    });
    return col;
  }
}
