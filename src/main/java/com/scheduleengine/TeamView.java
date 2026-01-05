package com.scheduleengine;

import com.scheduleengine.domain.Team;
import com.scheduleengine.domain.League;
import com.scheduleengine.service.TeamService;
import com.scheduleengine.service.LeagueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class TeamView {
    
    private final TeamService teamService;
    private final LeagueService leagueService;
    private TableView<Team> table;
    private ObservableList<Team> data;
    
    public TeamView(TeamService teamService, LeagueService leagueService) {
        this.teamService = teamService;
        this.leagueService = leagueService;
        this.data = FXCollections.observableArrayList();
    }
    
    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        HBox topBox = new HBox(10);
        Label title = new Label("Teams");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add Team");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());
        
        topBox.getChildren().addAll(title, spacer, refreshButton, addButton);
        
        table = new TableView<>();
        table.setItems(data);
        
        TableColumn<Team, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Team, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Team, String> coachCol = new TableColumn<>("Coach");
        coachCol.setCellValueFactory(new PropertyValueFactory<>("coach"));
        
        TableColumn<Team, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        
        table.getColumns().addAll(idCol, nameCol, coachCol, emailCol);
        
        loadData();
        
        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return vbox;
    }
    
    private void loadData() {
        data.clear();
        data.addAll(teamService.findAll());
    }
    
    private void showAddDialog() {
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle("Add Team");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        TextField coachField = new TextField();
        TextField emailField = new TextField();
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Coach:"), 0, 1);
        grid.add(coachField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Team team = new Team();
                team.setName(nameField.getText());
                team.setCoach(coachField.getText());
                team.setContactEmail(emailField.getText());
                return team;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(team -> {
            teamService.save(team);
            loadData();
        });
    }
}
