package com.scheduleengine.league;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.navigation.NavigationHandler;
import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.common.TablePreferencesUtil;
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
    private NavigationHandler navigationHandler;

    public LeagueView(LeagueService leagueService) {
        this.leagueService = leagueService;
        this.data = FXCollections.observableArrayList();
    }
    
    /**
     * Set the navigation handler for drill-down navigation
     */
    public void setNavigationHandler(NavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
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

        topBox.getChildren().addAll(title, spacer, addButton);

        // Table
        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<League, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setVisible(false);

        TableColumn<League, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<League, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);
        
        TableColumn<League, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");

            {
                viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    League league = getTableView().getItems().get(getIndex());
                    if (navigationHandler != null) {
                        com.scheduleengine.navigation.NavigationContext newContext =
                            new com.scheduleengine.navigation.NavigationContext()
                                .navigateTo("leagues", "Leagues")
                                .navigateTo("league-detail", league.getName(), league);
                        navigationHandler.navigate(newContext);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewBtn);
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, descCol, actionCol);
        
        // Setup column width persistence
        TablePreferencesUtil.setupTableColumnPersistence(table, "league.table");

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
        
        // Configure columns: label column fixed, field column grows
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(Region.USE_PREF_SIZE);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setMinWidth(350);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);
        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);
        descField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(descField, Priority.ALWAYS);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "league.add", 550, 400));

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

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
