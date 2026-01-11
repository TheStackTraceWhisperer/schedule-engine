package com.scheduleengine.field;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.service.FieldUsageBlockService;
import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.common.TablePreferencesUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.paint.Color;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class FieldView {
    
    private final FieldService fieldService;
    private final FieldAvailabilityService availabilityService;
    private final FieldUsageBlockService usageBlockService;
    private TableView<Field> table;
    private TableView<FieldAvailability> availabilityTable;
    private TableView<FieldUsageBlock> usageTable;
    private ObservableList<Field> data;
    private ComboBox<Field> utilizationFieldSelector;
    private GridPane utilizationGrid;

    private static final DayOfWeek[] DAYS = DayOfWeek.values();

    public FieldView(FieldService fieldService, FieldAvailabilityService availabilityService, FieldUsageBlockService usageBlockService) {
        this.fieldService = fieldService;
        this.availabilityService = availabilityService;
        this.usageBlockService = usageBlockService;
        this.data = FXCollections.observableArrayList();
    }
    
    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        HBox topBox = new HBox(10);
        Label title = new Label("Fields");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add Field");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());
        
        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteSelected.setOnAction(e -> deleteSelected());

        topBox.getChildren().addAll(title, spacer, refreshButton, addButton, deleteSelected);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Field, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Field, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);

        TableColumn<Field, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(160);

        TableColumn<Field, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(280);

        TableColumn<Field, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteField(getTableView().getItems().get(getIndex())));
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, editBtn, deleteBtn);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, locationCol, addressCol, actionCol);

        // Setup column width persistence (unique table id)
        TablePreferencesUtil.setupTableColumnPersistence(table, "field.table");

        TabPane tabs = new TabPane();
        Tab fieldsTab = new Tab("Fields", table);
        fieldsTab.setClosable(false);
        Tab hoursTab = new Tab("Hours of Operation", buildAvailabilityPane());
        hoursTab.setClosable(false);
        Tab blocksTab = new Tab("Dedicated Use Blocks", buildUsageBlocksPane());
        blocksTab.setClosable(false);
        Tab utilizationTab = new Tab("Weekly Utilization", buildWeeklyUtilizationPane());
        utilizationTab.setClosable(false);
        tabs.getTabs().addAll(fieldsTab, hoursTab, blocksTab, utilizationTab);

        vbox.getChildren().addAll(topBox, tabs);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        loadData();

        return vbox;
    }
    
    private VBox buildAvailabilityPane() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        availabilityTable = new TableView<>();
        TableColumn<FieldAvailability, String> fieldCol = new TableColumn<>("Field");
        fieldCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getField().getName()));
        TableColumn<FieldAvailability, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDayOfWeek().toString()));
        TableColumn<FieldAvailability, String> openCol = new TableColumn<>("Open");
        openCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getOpenTime().toString()));
        TableColumn<FieldAvailability, String> closeCol = new TableColumn<>("Close");
        closeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCloseTime().toString()));
        TableColumn<FieldAvailability, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            { edit.setOnAction(e -> showEditAvailability(getTableView().getItems().get(getIndex())));
              del.setOnAction(e -> deleteAvailability(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, edit, del));
            }
        });
        availabilityTable.getColumns().addAll(fieldCol, dayCol, openCol, closeCol, actionCol);
        Button add = new Button("Add Availability");
        add.setOnAction(e -> showAddAvailability());
        box.getChildren().addAll(availabilityTable, add);
        VBox.setVgrow(availabilityTable, Priority.ALWAYS);
        return box;
    }

    private VBox buildUsageBlocksPane() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        usageTable = new TableView<>();
        TableColumn<FieldUsageBlock, String> fieldCol = new TableColumn<>("Field");
        fieldCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getField().getName()));
        TableColumn<FieldUsageBlock, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDayOfWeek().toString()));
        TableColumn<FieldUsageBlock, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsageType().toString()));
        TableColumn<FieldUsageBlock, String> startCol = new TableColumn<>("Start");
        startCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStartTime().toString()));
        TableColumn<FieldUsageBlock, String> endCol = new TableColumn<>("End");
        endCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEndTime().toString()));
        TableColumn<FieldUsageBlock, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button del = new Button("Delete");
            { edit.setOnAction(e -> showEditBlock(getTableView().getItems().get(getIndex())));
              del.setOnAction(e -> deleteBlock(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, edit, del));
            }
        });
        usageTable.getColumns().addAll(fieldCol, dayCol, typeCol, startCol, endCol, actionCol);
        Button add = new Button("Add Dedicated Block");
        add.setOnAction(e -> showAddBlock());
        box.getChildren().addAll(usageTable, add);
        VBox.setVgrow(usageTable, Priority.ALWAYS);
        return box;
    }

    private VBox buildWeeklyUtilizationPane() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // Field selector
        utilizationFieldSelector = new ComboBox<>(FXCollections.observableArrayList(fieldService.findAll()));
        utilizationFieldSelector.setPromptText("Select Field");
        utilizationFieldSelector.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Field item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        utilizationFieldSelector.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Field item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        HBox header = new HBox(10, new Label("Field:"), utilizationFieldSelector);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Grid: 24 hours x 7 days (hours as rows, days as columns)
        utilizationGrid = new GridPane();
        utilizationGrid.setHgap(2);
        utilizationGrid.setVgap(2);
        utilizationGrid.setPadding(new Insets(10));

        // Row headers (hours) - 12-hour format with AM/PM
        for (int h = 0; h < 24; h++) {
            String labelText = to12HourLabel(h);
            Label hour = new Label(labelText);
            hour.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            GridPane.setConstraints(hour, 0, h + 1);
            utilizationGrid.getChildren().add(hour);
        }
        // Column headers (days) - abbreviated names
        for (int d = 0; d < DAYS.length; d++) {
            Label day = new Label(abbrev(DAYS[d]));
            day.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            GridPane.setConstraints(day, d + 1, 0);
            utilizationGrid.getChildren().add(day);
        }

        // Cells placeholders (will be re-rendered)
        for (int d = 0; d < DAYS.length; d++) {
            for (int h = 0; h < 24; h++) {
                Region cell = new Region();
                cell.setMinSize(32, 28);
                cell.setPrefSize(32, 28);
                cell.setMaxSize(32, 28);
                GridPane.setConstraints(cell, d + 1, h + 1);
                utilizationGrid.getChildren().add(cell);
            }
        }

        // Legend - vertical layout on the right
        VBox legend = new VBox(8);
        legend.setPadding(new Insets(10, 0, 0, 15));
        legend.getChildren().addAll(
            makeLegendSwatch("Available", Color.web("#43e97b")),
            makeLegendSwatch("League Block", Color.web("#667eea")),
            makeLegendSwatch("Tournament Block", Color.web("#fa709a")),
            makeLegendSwatch("Practice Block", Color.web("#feca57")),
            makeLegendSwatch("Unavailable", Color.web("#dfe6e9"))
        );

        // Main content area with grid on left and legend on right (top-aligned)
        HBox gridAndLegend = new HBox(10);
        gridAndLegend.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        gridAndLegend.getChildren().addAll(utilizationGrid, legend);

        container.getChildren().addAll(header, gridAndLegend);
        VBox.setVgrow(gridAndLegend, Priority.ALWAYS);

        // Update rendering when a field is selected
        utilizationFieldSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            renderUtilizationGrid(utilizationFieldSelector.getValue());
        });
        // Preselect first field if present
        if (!utilizationFieldSelector.getItems().isEmpty()) {
            utilizationFieldSelector.getSelectionModel().selectFirst();
            renderUtilizationGrid(utilizationFieldSelector.getValue());
        }

        return container;
    }

    private HBox makeLegendSwatch(String label, Color color) {
        Region swatch = new Region();
        swatch.setPrefSize(16, 16);
        swatch.setStyle("-fx-background-color: " + toCssColor(color) + "; -fx-border-color: #bdc3c7;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        return new HBox(6, swatch, l);
    }

    private void renderUtilizationGrid(Field field) {
        // Remove only cell regions (exclude headers at row 0 and col 0)
        utilizationGrid.getChildren().removeIf(node -> {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            return node instanceof Region && r != null && c != null && r > 0 && c > 0;
        });
        for (int d = 0; d < DAYS.length; d++) {
            for (int h = 0; h < 24; h++) {
                Region cell = new Region();
                cell.setMinSize(32, 28);
                cell.setPrefSize(32, 28);
                cell.setMaxSize(32, 28);
                String baseColor = "#dfe6e9"; // Unavailable by default
                String tooltipText = String.format("%s %02d:00", DAYS[d], h);
                if (field != null) {
                    // Availability windows for this day
                    var availability = availabilityService.findByFieldAndDayOfWeek(field, DAYS[d]);
                    int finalH = h;
                    boolean inAvailability = availability.stream().anyMatch(a -> {
                        int openMin = a.getOpenTime().getHour() * 60 + a.getOpenTime().getMinute();
                        int closeMin = a.getCloseTime().getHour() * 60 + a.getCloseTime().getMinute();
                        int cellMin = finalH * 60; // start of hour block
                        return cellMin >= openMin && cellMin < closeMin;
                    });
                    if (inAvailability) {
                        baseColor = "#43e97b";
                        tooltipText += "\nAvailable";
                    } else {
                        tooltipText += "\nUnavailable";
                    }

                    // Dedicated usage blocks overlay
                    var blocks = usageBlockService.findByFieldAndDayOfWeek(field, DAYS[d]);
                    for (var b : blocks) {
                        int startMin = b.getStartTime().getHour() * 60 + b.getStartTime().getMinute();
                        int endMin = b.getEndTime().getHour() * 60 + b.getEndTime().getMinute();
                        int cellMin = h * 60;
                        boolean inBlock = cellMin >= startMin && cellMin < endMin;
                        if (inBlock) {
                            switch (b.getUsageType()) {
                                case LEAGUE -> { baseColor = "#667eea"; tooltipText += "\nLeague Block"; }
                                case TOURNAMENT -> { baseColor = "#fa709a"; tooltipText += "\nTournament Block"; }
                                case PRACTICE -> { baseColor = "#feca57"; tooltipText += "\nPractice Block"; }
                                case CLOSED -> { baseColor = "#dfe6e9"; tooltipText += "\nClosed"; }
                            }
                        }
                    }
                }
                cell.setStyle("-fx-background-color: " + baseColor + "; -fx-border-color: #ffffff; -fx-border-width: 1;");
                Tooltip tp = new Tooltip(tooltipText);
                Tooltip.install(cell, tp);
                GridPane.setConstraints(cell, d + 1, h + 1);
                utilizationGrid.getChildren().add(cell);
            }
        }
    }

    private String toCssColor(Color c) {
        int r = (int)(c.getRed() * 255);
        int g = (int)(c.getGreen() * 255);
        int b = (int)(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private void loadData() {
        data.clear();
        data.addAll(fieldService.findAll());
        if (availabilityTable != null && usageTable != null) {
            var fields = fieldService.findAll();
            availabilityTable.getItems().setAll(fields.stream().flatMap(f -> availabilityService.findByField(f).stream()).toList());
            usageTable.getItems().setAll(fields.stream().flatMap(f -> usageBlockService.findByField(f).stream()).toList());
        }
        if (utilizationFieldSelector != null) {
            var items = FXCollections.observableArrayList(fieldService.findAll());
            utilizationFieldSelector.setItems(items);
            if (!items.isEmpty() && utilizationFieldSelector.getValue() == null) {
                utilizationFieldSelector.getSelectionModel().selectFirst();
            }
            // Re-render current selection
            renderUtilizationGrid(utilizationFieldSelector.getValue());
        }
    }
    
    private void showAddDialog() {
        Dialog<Field> dialog = new Dialog<>();
        dialog.setTitle("Add Field");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Configure columns: label column fixed, field column grows
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(Region.USE_PREF_SIZE);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setMinWidth(350);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);
        TextField locationField = new TextField();
        locationField.setMaxWidth(Double.MAX_VALUE);
        TextField addressField = new TextField();
        addressField.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "field.add", 600, 400));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
                    showError("Validation", "Name is required");
                    return null;
                }
                Field field = new Field();
                field.setName(nameField.getText());
                field.setLocation(locationField.getText());
                field.setAddress(addressField.getText());
                return field;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(field -> {
            fieldService.save(field);
            loadData();
        });
    }

    private void showEditDialog(Field field) {
        Dialog<Field> dialog = new Dialog<>();
        dialog.setTitle("Edit Field");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Configure columns: label column fixed, field column grows
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(Region.USE_PREF_SIZE);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setMinWidth(350);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField(field.getName());
        nameField.setMaxWidth(Double.MAX_VALUE);
        TextField locationField = new TextField(field.getLocation());
        locationField.setMaxWidth(Double.MAX_VALUE);
        TextField addressField = new TextField(field.getAddress());
        addressField.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "field.edit", 600, 400));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
                    showError("Validation", "Name is required");
                    return null;
                }
                field.setName(nameField.getText());
                field.setLocation(locationField.getText());
                field.setAddress(addressField.getText());
                return field;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            fieldService.update(field.getId(), updated);
            loadData();
        });
    }

    private void deleteField(Field field) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Field");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the field: " + field.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                fieldService.deleteById(field.getId());
                loadData();
            }
        });
    }

    private void deleteSelected() {
        var selected = table.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showError("Delete Fields", "Select one or more fields to delete.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Fields");
        alert.setHeaderText("Delete " + selected.size() + " fields?");
        alert.setContentText("This will remove the selected fields.");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                var toDelete = FXCollections.observableArrayList(selected);
                toDelete.forEach(f -> fieldService.deleteById(f.getId()));
                loadData();
            }
        });
    }

    private void showAddAvailability() {
        Dialog<FieldAvailability> dialog = new Dialog<>();
        dialog.setTitle("Add Field Availability");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        ComboBox<Field> fieldCombo = new ComboBox<>(FXCollections.observableArrayList(fieldService.findAll()));
        // Display field names in dropdown
        fieldCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Field item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        fieldCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Field item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        ComboBox<DayOfWeek> dayCombo = new ComboBox<>(FXCollections.observableArrayList(DayOfWeek.values()));
        Spinner<Integer> openH = new Spinner<>(0,23,9); Spinner<Integer> openM = new Spinner<>(0,59,0,5);
        Spinner<Integer> closeH = new Spinner<>(0,23,21); Spinner<Integer> closeM = new Spinner<>(0,59,0,5);
        grid.add(new Label("Field:"),0,0); grid.add(fieldCombo,1,0);
        grid.add(new Label("Day:"),0,1); grid.add(dayCombo,1,1);
        grid.add(new Label("Open:"),0,2); grid.add(new HBox(6, openH, new Label(":"), openM),1,2);
        grid.add(new Label("Close:"),0,3); grid.add(new HBox(6, closeH, new Label(":"), closeM),1,3);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && fieldCombo.getValue()!=null && dayCombo.getValue()!=null) {
                return new FieldAvailability(fieldCombo.getValue(), dayCombo.getValue(),
                    LocalTime.of(openH.getValue(), openM.getValue()), LocalTime.of(closeH.getValue(), closeM.getValue()));
            }
            return null;
        });
        dialog.showAndWait().ifPresent(availabilityService::save);
        refresh();
    }

    private void showEditAvailability(FieldAvailability fa) {
        Dialog<FieldAvailability> dialog = new Dialog<>();
        dialog.setTitle("Edit Field Availability");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        Spinner<Integer> openH = new Spinner<>(0,23,fa.getOpenTime().getHour()); Spinner<Integer> openM = new Spinner<>(0,59,fa.getOpenTime().getMinute(),5);
        Spinner<Integer> closeH = new Spinner<>(0,23,fa.getCloseTime().getHour()); Spinner<Integer> closeM = new Spinner<>(0,59,fa.getCloseTime().getMinute(),5);
        grid.add(new Label("Open:"),0,0); grid.add(new HBox(6, openH, new Label(":"), openM),1,0);
        grid.add(new Label("Close:"),0,1); grid.add(new HBox(6, closeH, new Label(":"), closeM),1,1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                fa.setOpenTime(LocalTime.of(openH.getValue(), openM.getValue()));
                fa.setCloseTime(LocalTime.of(closeH.getValue(), closeM.getValue()));
                return fa;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(availabilityService::save);
        refresh();
    }

    private void deleteAvailability(FieldAvailability fa) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete availability?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().ifPresent(btn -> { if (btn==ButtonType.OK) { availabilityService.delete(fa.getId()); refresh(); renderUtilizationGrid(utilizationFieldSelector != null ? utilizationFieldSelector.getValue() : null); }});
    }

    private void showAddBlock() {
        Dialog<FieldUsageBlock> dialog = new Dialog<>();
        dialog.setTitle("Add Dedicated Use Block");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        ComboBox<Field> fieldCombo = new ComboBox<>(FXCollections.observableArrayList(fieldService.findAll()));
        // Display field names in dropdown
        fieldCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Field item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        fieldCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Field item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        ComboBox<DayOfWeek> dayCombo = new ComboBox<>(FXCollections.observableArrayList(DayOfWeek.values()));
        ComboBox<FieldUsageBlock.UsageType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(FieldUsageBlock.UsageType.values()));
        Spinner<Integer> startH = new Spinner<>(0,23,9); Spinner<Integer> startM = new Spinner<>(0,59,0,5);
        Spinner<Integer> endH = new Spinner<>(0,23,12); Spinner<Integer> endM = new Spinner<>(0,59,0,5);
        TextField notesField = new TextField();
        grid.add(new Label("Field:"),0,0); grid.add(fieldCombo,1,0);
        grid.add(new Label("Day:"),0,1); grid.add(dayCombo,1,1);
        grid.add(new Label("Type:"),0,2); grid.add(typeCombo,1,2);
        grid.add(new Label("Start:"),0,3); grid.add(new HBox(6, startH, new Label(":"), startM),1,3);
        grid.add(new Label("End:"),0,4); grid.add(new HBox(6, endH, new Label(":"), endM),1,4);
        grid.add(new Label("Notes:"),0,5); grid.add(notesField,1,5);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && fieldCombo.getValue()!=null && dayCombo.getValue()!=null && typeCombo.getValue()!=null) {
                return new FieldUsageBlock(fieldCombo.getValue(), dayCombo.getValue(), typeCombo.getValue(),
                    LocalTime.of(startH.getValue(), startM.getValue()), LocalTime.of(endH.getValue(), endM.getValue()), notesField.getText());
            }
            return null;
        });
        dialog.showAndWait().ifPresent(usageBlockService::save);
        refresh();
    }

    private void showEditBlock(FieldUsageBlock b) {
        Dialog<FieldUsageBlock> dialog = new Dialog<>();
        dialog.setTitle("Edit Dedicated Use Block");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        ComboBox<FieldUsageBlock.UsageType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(FieldUsageBlock.UsageType.values()));
        typeCombo.setValue(b.getUsageType());
        Spinner<Integer> startH = new Spinner<>(0,23,b.getStartTime().getHour()); Spinner<Integer> startM = new Spinner<>(0,59,b.getStartTime().getMinute(),5);
        Spinner<Integer> endH = new Spinner<>(0,23,b.getEndTime().getHour()); Spinner<Integer> endM = new Spinner<>(0,59,b.getEndTime().getMinute(),5);
        TextField notesField = new TextField(b.getNotes());
        grid.add(new Label("Type:"),0,0); grid.add(typeCombo,1,0);
        grid.add(new Label("Start:"),0,1); grid.add(new HBox(6, startH, new Label(":"), startM),1,1);
        grid.add(new Label("End:"),0,2); grid.add(new HBox(6, endH, new Label(":"), endM),1,2);
        grid.add(new Label("Notes:"),0,3); grid.add(notesField,1,3);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                b.setUsageType(typeCombo.getValue());
                b.setStartTime(LocalTime.of(startH.getValue(), startM.getValue()));
                b.setEndTime(LocalTime.of(endH.getValue(), endM.getValue()));
                b.setNotes(notesField.getText());
                return b;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(usageBlockService::save);
        refresh();
    }

    private void deleteBlock(FieldUsageBlock b) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete block?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().ifPresent(btn -> { if (btn==ButtonType.OK) { usageBlockService.delete(b.getId()); refresh(); renderUtilizationGrid(utilizationFieldSelector != null ? utilizationFieldSelector.getValue() : null); }});
    }

    public void refresh() {
        loadData();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String abbrev(java.time.DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Mon";
            case TUESDAY -> "Tue";
            case WEDNESDAY -> "Wed";
            case THURSDAY -> "Thu";
            case FRIDAY -> "Fri";
            case SATURDAY -> "Sat";
            case SUNDAY -> "Sun";
        };
    }

    private String to12HourLabel(int hour24) {
        int h = hour24 % 12;
        if (h == 0) h = 12;
        String period = hour24 < 12 ? "AM" : "PM";
        return h + " " + period;
    }
}
