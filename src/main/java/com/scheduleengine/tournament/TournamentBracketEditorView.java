package com.scheduleengine.tournament;

import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.domain.TournamentRegistration;
import com.scheduleengine.tournament.service.TournamentService;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;

public class TournamentBracketEditorView {

    private final TournamentService tournamentService;
    private final TournamentRegistrationService registrationService;
    private Tournament tournament;
    private ObservableList<TournamentRegistration> registrations = FXCollections.observableArrayList();

    public TournamentBracketEditorView(TournamentService tournamentService,
                                      TournamentRegistrationService registrationService) {
        this.tournamentService = tournamentService;
        this.registrationService = registrationService;
    }

    public VBox getView(Tournament tournament) {
        this.tournament = tournament;
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label("Tournament Bracket - " + tournament.getName());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label typeLabel = new Label(tournament.getType().toString());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #667eea; -fx-padding: 0 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button seedBtn = new Button("Auto-Seed Teams");
        seedBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        seedBtn.setOnAction(e -> autoSeedTeams());

        Button generateBtn = new Button("Generate Bracket");
        generateBtn.setStyle("-fx-background-color: #43e97b; -fx-text-fill: white;");
        generateBtn.setOnAction(e -> generateBracket());

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> vbox.getChildren().clear());

        headerBox.getChildren().addAll(titleLabel, typeLabel, spacer, seedBtn, generateBtn, closeBtn);

        // Tournament info
        HBox infoBox = new HBox(30);
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

        Label statusInfo = new Label("Status: " + tournament.getStatus());
        Label datesInfo = new Label("Dates: " + tournament.getStartDate() + " to " + tournament.getEndDate());
        Label teamsInfo = new Label("Teams: " + registrationService.findByTournamentId(tournament.getId()).size() +
                                   (tournament.getMaxTeams() != null ? "/" + tournament.getMaxTeams() : ""));

        infoBox.getChildren().addAll(statusInfo, datesInfo, teamsInfo);

        // Registrations table
        TableView<TournamentRegistration> regTable = new TableView<>();
        loadRegistrations();
        regTable.setItems(registrations);
        regTable.setPrefHeight(400);

        TableColumn<TournamentRegistration, Integer> seedCol = new TableColumn<>("Seed");
        seedCol.setCellValueFactory(new PropertyValueFactory<>("seedNumber"));
        seedCol.setPrefWidth(60);
        seedCol.setEditable(true);
        seedCol.setCellFactory(TextFieldTableCell.forTableColumn(
            new javafx.util.converter.IntegerStringConverter() {
                @Override
                public Integer fromString(String value) {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        ));

        TableColumn<TournamentRegistration, String> teamCol = new TableColumn<>("Team");
        teamCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getTeam().getName())
        );
        teamCol.setPrefWidth(200);

        TableColumn<TournamentRegistration, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus().toString())
        );
        statusCol.setPrefWidth(120);

        TableColumn<TournamentRegistration, String> regDateCol = new TableColumn<>("Registered");
        regDateCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRegistrationDate().toLocalDate().toString())
        );
        regDateCol.setPrefWidth(120);

        regTable.getColumns().addAll(seedCol, teamCol, statusCol, regDateCol);
        regTable.setEditable(true);

        Label bracketInfo = new Label("Registered teams will be seeded into the tournament bracket. Edit seed numbers to adjust team positions.");
        bracketInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 10;");
        bracketInfo.setWrapText(true);

        vbox.getChildren().addAll(headerBox, infoBox, new Separator(), bracketInfo, regTable);
        VBox.setVgrow(regTable, Priority.ALWAYS);

        return vbox;
    }

    private void loadRegistrations() {
        registrations.clear();
        var approved = registrationService.findByTournamentIdAndStatus(
            tournament.getId(),
            TournamentRegistration.RegistrationStatus.APPROVED
        );
        registrations.addAll(approved);
    }

    private void autoSeedTeams() {
        int seed = 1;
        for (TournamentRegistration reg : registrations) {
            reg.setSeedNumber(seed++);
            registrationService.save(reg);
        }
        showInfo("Success", "Teams auto-seeded (1-" + registrations.size() + ")");
    }

    private void generateBracket() {
        if (registrations.isEmpty()) {
            showError("Error", "No teams registered for this tournament");
            return;
        }

        // Validate seeding
        for (TournamentRegistration reg : registrations) {
            if (reg.getSeedNumber() == null) {
                showError("Error", "All teams must have a seed number. Use 'Auto-Seed Teams' first.");
                return;
            }
        }

        showInfo("Success", "Bracket generated for " + registrations.size() + " teams. " +
                           "Bracket structure will depend on team count.\n\n" +
                           "Note: Detailed bracket visualization coming in next update.");
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

