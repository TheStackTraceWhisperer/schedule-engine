package com.scheduleengine.common;

import com.scheduleengine.season.domain.Season;
import com.scheduleengine.common.service.ScheduleGeneratorService;
import com.scheduleengine.game.service.GameService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;

public class ScheduleGeneratorResultView {

    private final ScheduleGeneratorService scheduleGeneratorService;
    private final GameService gameService;
    private Season season;
    private ObservableList<ScheduleGeneratorService.Round> rounds = FXCollections.observableArrayList();

    public ScheduleGeneratorResultView(ScheduleGeneratorService scheduleGeneratorService, GameService gameService) {
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.gameService = gameService;
    }

    public VBox getView(Season season) {
        this.season = season;
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label("Schedule Generator - " + (season != null ? season.getName() : "No Season"));
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button generateBtn = new Button("Generate Schedule");
        generateBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        generateBtn.setOnAction(e -> generateSchedule());

        Button saveBtn = new Button("Save to Database");
        saveBtn.setStyle("-fx-background-color: #43e97b; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> saveSchedule());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        cancelBtn.setOnAction(e -> vbox.getChildren().clear());

        headerBox.getChildren().addAll(titleLabel, spacer, generateBtn, saveBtn, cancelBtn);

        // Rounds table
        TableView<ScheduleGeneratorService.Round> roundsTable = new TableView<>();
        roundsTable.setItems(rounds);
        roundsTable.setPrefHeight(400);

        TableColumn<ScheduleGeneratorService.Round, Integer> roundCol = new TableColumn<>("Round");
        roundCol.setCellValueFactory(new PropertyValueFactory<>("roundNumber"));
        roundCol.setPrefWidth(80);

        TableColumn<ScheduleGeneratorService.Round, String> matchesCol = new TableColumn<>("Matches");
        matchesCol.setCellValueFactory(cellData -> {
            int count = cellData.getValue().getMatches().size();
            return new SimpleStringProperty(count + " matches");
        });
        matchesCol.setPrefWidth(120);

        TableColumn<ScheduleGeneratorService.Round, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(cellData -> {
            StringBuilder sb = new StringBuilder();
            for (ScheduleGeneratorService.Match m : cellData.getValue().getMatches()) {
                sb.append(m.getHome().getName()).append(" vs ").append(m.getAway().getName()).append(" | ");
            }
            String result = sb.toString();
            if (result.length() > 100) {
                result = result.substring(0, 97) + "...";
            }
            return new SimpleStringProperty(result);
        });
        detailsCol.setPrefWidth(400);

        roundsTable.getColumns().addAll(roundCol, matchesCol, detailsCol);

        Label infoLabel = new Label("Click 'Generate Schedule' to create a round-robin schedule for this season.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 10;");
        infoLabel.setWrapText(true);

        vbox.getChildren().addAll(headerBox, infoLabel, new Separator(), roundsTable);
        VBox.setVgrow(roundsTable, Priority.ALWAYS);

        return vbox;
    }

    private void generateSchedule() {
        if (season == null || season.getLeague() == null) {
            showError("Error", "Season or League is missing");
            return;
        }

        rounds.clear();
        rounds.addAll(scheduleGeneratorService.generateRoundRobin(season.getLeague(), season));

        if (rounds.isEmpty()) {
            showInfo("Result", "No schedule generated. Check that the league has enough teams.");
        } else {
            showInfo("Success", "Generated " + rounds.size() + " rounds with " +
                    rounds.stream().mapToInt(r -> r.getMatches().size()).sum() + " matches");
        }
    }

    private void saveSchedule() {
        if (rounds.isEmpty()) {
            showError("Error", "No schedule to save. Generate one first.");
            return;
        }

        try {
            scheduleGeneratorService.generateAndPersist(season.getLeague(), season, true);
            showInfo("Success", "Schedule saved to database");
        } catch (Exception e) {
            showError("Error", "Failed to save: " + e.getMessage());
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

