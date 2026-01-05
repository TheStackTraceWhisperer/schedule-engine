package com.scheduleengine;

import com.scheduleengine.domain.Game;
import com.scheduleengine.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class GameView {
    
    private final GameService gameService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private TableView<Game> table;
    private ObservableList<Game> data;
    
    public GameView(GameService gameService, TeamService teamService,
                   FieldService fieldService, SeasonService seasonService) {
        this.gameService = gameService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.data = FXCollections.observableArrayList();
    }
    
    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        HBox topBox = new HBox(10);
        Label title = new Label("Games");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add Game");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());
        
        topBox.getChildren().addAll(title, spacer, refreshButton, addButton);
        
        table = new TableView<>();
        table.setItems(data);
        
        TableColumn<Game, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Game, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("gameDate"));
        dateCol.setPrefWidth(150);
        
        TableColumn<Game, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        table.getColumns().addAll(idCol, dateCol, statusCol);
        
        loadData();
        
        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return vbox;
    }
    
    private void loadData() {
        data.clear();
        data.addAll(gameService.findAll());
    }
    
    private void showAddDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Game");
        alert.setContentText("Game creation form would go here");
        alert.showAndWait();
    }
}
