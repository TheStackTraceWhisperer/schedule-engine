package com.scheduleengine.common;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class TablePreferencesUtilTest {

  private TableView<Person> table;

  @Start
  public void start(Stage stage) {
    table = new TableView<>();
    TableColumn<Person, Long> id = new TableColumn<>("ID");
    TableColumn<Person, String> name = new TableColumn<>("Name");
    TableColumn<Person, Number> age = new TableColumn<>("Age");
    table.getColumns().addAll(id, name, age);

    VBox root = new VBox(table);
    stage.setScene(new Scene(root, 800, 600));
    stage.show();

    TablePreferencesUtil.bind(table, "person.test");
    TablePreferencesUtil.attachToggleMenu(table, "person.test");
  }

  @Test
  void toggleMenuAttachedAndSortBindingWorks() {
    // Context menu for toggling columns should be present
    ContextMenu cm = table.getContextMenu();
    assertNotNull(cm, "Context menu should be attached by attachToggleMenu");
    assertFalse(cm.getItems().isEmpty(), "Toggle menu should have items for columns");

    TableColumn<Person, ?> nameCol = table.getColumns().stream().filter(c -> "Name".equals(c.getText())).findFirst().orElseThrow();

    // Ensure sort manipulation happens on FX thread
    Platform.runLater(() -> {
      nameCol.setSortType(TableColumn.SortType.ASCENDING);
      table.getSortOrder().setAll(nameCol);
      table.sort();
    });
    WaitForAsyncUtils.waitForFxEvents();

    assertTrue(table.getSortOrder().contains(nameCol));
  }

  // Simple POJO for table model
  static class Person {
    Long id;
    String name;
    Integer age;
  }
}
