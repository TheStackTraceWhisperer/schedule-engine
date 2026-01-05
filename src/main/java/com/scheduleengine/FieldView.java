package com.scheduleengine;

import com.scheduleengine.domain.Field;
import com.scheduleengine.service.FieldService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class FieldView {
    
    private final FieldService fieldService;
    private TableView<Field> table;
    private ObservableList<Field> data;
    
    public FieldView(FieldService fieldService) {
        this.fieldService = fieldService;
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
        
        topBox.getChildren().addAll(title, spacer, refreshButton, addButton);
        
        table = new TableView<>();
        table.setItems(data);
        
        TableColumn<Field, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Field, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Field, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        TableColumn<Field, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        table.getColumns().addAll(idCol, nameCol, locationCol, addressCol);
        
        loadData();
        
        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return vbox;
    }
    
    private void loadData() {
        data.clear();
        data.addAll(fieldService.findAll());
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
        
        TextField nameField = new TextField();
        TextField locationField = new TextField();
        TextField addressField = new TextField();
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
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
}
