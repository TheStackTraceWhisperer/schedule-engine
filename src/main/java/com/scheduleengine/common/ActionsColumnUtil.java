package com.scheduleengine.common;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.function.Consumer;

/**
 * Shared factory for standard Actions table columns.
 */
public final class ActionsColumnUtil {
  private ActionsColumnUtil() {}

  /**
   * Create an actions column with a single "View Details" button.
   * @param <T> row type
   * @param title column title
   * @param onView callback invoked with the row on click
   * @param prefWidth preferred width
   * @return TableColumn<T, Void>
   */
  public static <T> TableColumn<T, Void> viewDetailsColumn(String title, Consumer<T> onView, double prefWidth) {
    TableColumn<T, Void> col = new TableColumn<>(title);
    col.setPrefWidth(prefWidth);
    col.setCellFactory(new Callback<>() {
      @Override
      public TableCell<T, Void> call(TableColumn<T, Void> param) {
        return new TableCell<>() {
          private final Button viewBtn = new Button("View Details");
          {
            viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
            viewBtn.setOnAction(e -> {
              T row = getTableView().getItems().get(getIndex());
              onView.accept(row);
            });
          }
          @Override
          protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : new HBox(6, viewBtn));
          }
        };
      }
    });
    return col;
  }
}
