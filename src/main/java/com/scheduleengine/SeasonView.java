package com.scheduleengine;

import com.scheduleengine.domain.Season;
import com.scheduleengine.service.SeasonService;
import com.scheduleengine.service.LeagueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class SeasonView {
    
    private final SeasonService seasonService;
    private final LeagueService leagueService;
    private TableView<Season> table;
    private ObservableList<Season> data;
    
    public SeasonView(SeasonService seasonService, LeagueService leagueService) {
        this.seasonService = seasonService;
        this.leagueService = leagueService;
        this.data = FXCollections.observableArrayList();
    }
    
    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        HBox topBox = new HBox(10);
        Label title = new Label("Seasons");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add Season");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());
        
        topBox.getChildren().addAll(title, spacer, refreshButton, addButton);
        
        table = new TableView<>();
        table.setItems(data);
        
        TableColumn<Season, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Season, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Season, String> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        
        TableColumn<Season, String> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        table.getColumns().addAll(idCol, nameCol, startCol, endCol);
        
        loadData();
        
        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return vbox;
    }
    
    private void loadData() {
        data.clear();
        data.addAll(seasonService.findAll());
    }
    
    private void showAddDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Season");
        alert.setContentText("Season creation form would go here");
        alert.showAndWait();
    }
}
