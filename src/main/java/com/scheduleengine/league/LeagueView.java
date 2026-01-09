package com.scheduleengine.league;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.control.SelectionMode;

public class LeagueView {
    
    private final LeagueService leagueService;
    private TableView<League> table;
    private ObservableList<League> data;
    
    public LeagueView(LeagueService leagueService) {
        this.leagueService = leagueService;
        this.data = FXCollections.observableArrayList();
    }
    
    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        // Title and buttons
        HBox topBox = new HBox(10);
        Label title = new Label("Leagues");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add League");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());
        
        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteSelected.setOnAction(e -> deleteSelected());

        topBox.getChildren().addAll(title, spacer, refreshButton, addButton, deleteSelected);

        // Table
        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<League, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<League, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<League, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);
        
        TableColumn<League, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            
            {
                editBtn.setOnAction(e -> {
                    League league = getTableView().getItems().get(getIndex());
                    showEditDialog(league);
                });
                deleteBtn.setOnAction(e -> {
                    League league = getTableView().getItems().get(getIndex());
                    deleteLeague(league);
                });
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, descCol, actionCol);
        
        loadData();
        
        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return vbox;
    }
    
    public void refresh() {
        loadData();
    }

    private void loadData() {
        data.clear();
        data.addAll(leagueService.findAll());
    }
    
    private void showAddDialog() {
        Dialog<League> dialog = new Dialog<>();
        dialog.setTitle("Add League");
        dialog.setHeaderText("Create a new league");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                League league = new League();
                league.setName(nameField.getText());
                league.setDescription(descField.getText());
                return league;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(league -> {
            leagueService.save(league);
            loadData();
        });
    }
    
    private void showEditDialog(League league) {
        Dialog<League> dialog = new Dialog<>();
        dialog.setTitle("Edit League");
        dialog.setHeaderText("Edit league information");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(league.getName());
        TextArea descField = new TextArea(league.getDescription());
        descField.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                league.setName(nameField.getText());
                league.setDescription(descField.getText());
                return league;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(updatedLeague -> {
            leagueService.update(league.getId(), updatedLeague);
            loadData();
        });
    }
    
    private void deleteLeague(League league) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete League");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the league: " + league.getName() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                leagueService.deleteById(league.getId());
                loadData();
            }
        });
    }

    private void deleteSelected() {
        var selected = table.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showError("Delete Leagues", "Select one or more leagues to delete.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Leagues");
        alert.setHeaderText("Delete " + selected.size() + " leagues?");
        alert.setContentText("This will remove the selected leagues.");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                var toDelete = FXCollections.observableArrayList(selected);
                toDelete.forEach(l -> leagueService.deleteById(l.getId()));
                loadData();
            }
        });
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
