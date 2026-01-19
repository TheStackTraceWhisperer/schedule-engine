package com.scheduleengine.field;

import com.scheduleengine.common.ActionsColumnUtil;
import com.scheduleengine.common.IconBadge;
import com.scheduleengine.common.IconPicker;
import com.scheduleengine.common.TableIconColumns;
import com.scheduleengine.common.TablePreferencesUtil;
import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.service.FieldUsageBlockService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FieldView {

  private final FieldService fieldService;
  private final FieldAvailabilityService availabilityService;
  private final FieldUsageBlockService usageBlockService;
  private final GameService gameService;
  private NavigationHandler navigationHandler;
  private TableView<Field> table;
  private final ObservableList<Field> data;

  public FieldView(FieldService fieldService, FieldAvailabilityService availabilityService,
                   FieldUsageBlockService usageBlockService, GameService gameService) {
    this.fieldService = fieldService;
    this.availabilityService = availabilityService;
    this.usageBlockService = usageBlockService;
    this.gameService = gameService;
    this.data = FXCollections.observableArrayList();
  }

  public void setNavigationHandler(NavigationHandler navigationHandler) {
    this.navigationHandler = navigationHandler;
  }

  public void refresh() {
    loadData();
  }

  public VBox getView() {
    VBox vbox = new VBox(10);
    vbox.setPadding(new Insets(10));

    HBox topBox = new HBox(10);
    javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Fields");
    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button addBtn = new Button("Add Field");
    addBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
    addBtn.setOnAction(e -> showAddDialog());

    Button refreshButton = new Button("Refresh");
    refreshButton.setOnAction(e -> refresh());

    Button deleteButton = new Button("Delete Selected");
    deleteButton.setOnAction(e -> deleteSelectedField());

    topBox.getChildren().addAll(titleLabel, spacer, addBtn, refreshButton, deleteButton);

    table = new TableView<>();
    table.setItems(data);
    table.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);

    TableColumn<Field, Void> iconCol = TableIconColumns.iconColumn(
      "Icon",
      Field::getIconName,
      Field::getIconBackgroundColor,
      Field::getIconGlyphColor,
      32, 64
    );

    TableColumn<Field, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setPrefWidth(150);

    TableColumn<Field, String> locationCol = new TableColumn<>("Location");
    locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
    locationCol.setPrefWidth(150);

    TableColumn<Field, String> addressCol = new TableColumn<>("Address");
    addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
    addressCol.setPrefWidth(200);

    TableColumn<Field, Void> actionCol = ActionsColumnUtil.viewDetailsColumn("Actions", this::viewFieldDetails, 150);

    table.getColumns().addAll(iconCol, nameCol, locationCol, addressCol, actionCol);
    TablePreferencesUtil.bind(table, "field");
    TablePreferencesUtil.attachToggleMenu(table, "field");
    TablePreferencesUtil.setupTableColumnPersistence(table, "field.table");

    vbox.getChildren().addAll(topBox, table);
    VBox.setVgrow(table, Priority.ALWAYS);

    loadData();

    return vbox;
  }

  private void loadData() {
    data.clear();
    data.addAll(fieldService.findAll());
  }

  private void viewFieldDetails(Field field) {
    if (navigationHandler != null) {
      NavigationContext newContext = new NavigationContext()
        .navigateTo("fields", "Fields")
        .navigateTo("field-detail", field.getName(), field);
      navigationHandler.navigate(newContext);
    }
  }

  private void showAddDialog() {
    javafx.scene.control.Dialog<Field> dialog = new javafx.scene.control.Dialog<>();
    dialog.setTitle("Add Field");

    javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
    nameField.setPromptText("Field Name");
    javafx.scene.control.TextField locationField = new javafx.scene.control.TextField();
    locationField.setPromptText("Location");
    javafx.scene.control.TextField addressField = new javafx.scene.control.TextField();
    addressField.setPromptText("Address");

    javafx.scene.control.Label iconLabel = new javafx.scene.control.Label("Icon:");
    IconPicker iconPicker = new IconPicker(null, null, null, null);

    grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new javafx.scene.control.Label("Location:"), 0, 1);
    grid.add(locationField, 1, 1);
    grid.add(new javafx.scene.control.Label("Address:"), 0, 2);
    grid.add(addressField, 1, 2);
    grid.add(iconLabel, 0, 3);
    grid.add(iconPicker, 1, 3);

    dialog.getDialogPane().setContent(grid);
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType) {
        Field field = new Field();
        field.setName(nameField.getText());
        field.setLocation(locationField.getText());
        field.setAddress(addressField.getText());
        IconPicker.Selection sel = iconPicker.currentSelection();
        field.setIconName(sel.iconName);
        field.setIconBackgroundColor(sel.bgColor);
        field.setIconGlyphColor(sel.glyphColor);
        return field;
      }
      return null;
    });

    dialog.showAndWait().ifPresent(field -> {
      fieldService.save(field);
      loadData();
    });
  }

  private void deleteSelectedField() {
    // TODO: Implement this method
  }
}
