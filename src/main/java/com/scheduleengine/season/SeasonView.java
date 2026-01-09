package com.scheduleengine.season;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.game.GameView;
import com.scheduleengine.common.service.ScheduleGeneratorService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class SeasonView {
    
    private final SeasonService seasonService;
    private final LeagueService leagueService;
    private TableView<Season> table;
    private ObservableList<Season> data;
    private ScheduleGeneratorService scheduleService;
    private final GameView gameView; // to refresh after generation

    public SeasonView(SeasonService seasonService, LeagueService leagueService, ScheduleGeneratorService scheduleService, GameView gameView) {
        this.seasonService = seasonService;
        this.leagueService = leagueService;
        this.scheduleService = scheduleService;
        this.gameView = gameView;
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
        
        CheckBox overwriteBox = new CheckBox("Overwrite existing games");
        overwriteBox.setSelected(true);

        Button genButton = new Button("Generate & Save Schedule");
        genButton.setOnAction(e -> onGenerateSchedule(overwriteBox.isSelected()));

        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteSelected.setOnAction(e -> deleteSelected());

        topBox.getChildren().addAll(title, spacer, overwriteBox, genButton, refreshButton, addButton, deleteSelected);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Season, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Season, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Season, LocalDate> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(140);

        TableColumn<Season, LocalDate> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endCol.setPrefWidth(140);

        TableColumn<Season, String> leagueCol = new TableColumn<>("League");
        leagueCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
            cell.getValue().getLeague() != null ? cell.getValue().getLeague().getName() : ""
        ));
        leagueCol.setPrefWidth(200);

        TableColumn<Season, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteSeason(getTableView().getItems().get(getIndex())));
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

        table.getColumns().addAll(idCol, nameCol, startCol, endCol, leagueCol, actionCol);

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
        data.addAll(seasonService.findAll());
    }
    
    private void showAddDialog() {
        Dialog<Season> dialog = new Dialog<>();
        dialog.setTitle("Add Season");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        leagueCombo.setConverter(new StringConverter<>() {
            @Override public String toString(League l) { return l == null ? "" : l.getName(); }
            @Override public League fromString(String s) { return null; }
        });
        leagueCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(League item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startPicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endPicker, 1, 2);
        grid.add(new Label("League:"), 0, 3);
        grid.add(leagueCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                if (nameField.getText().isBlank() || startPicker.getValue() == null || endPicker.getValue() == null) {
                    showError("Validation", "Name, start date and end date are required");
                    return null;
                }
                Season s = new Season();
                s.setName(nameField.getText());
                s.setStartDate(startPicker.getValue());
                s.setEndDate(endPicker.getValue());
                s.setLeague(leagueCombo.getValue());
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(season -> {
            seasonService.save(season);
            loadData();
        });
    }

    private void showEditDialog(Season season) {
        Dialog<Season> dialog = new Dialog<>();
        dialog.setTitle("Edit Season");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(season.getName());
        DatePicker startPicker = new DatePicker(season.getStartDate());
        DatePicker endPicker = new DatePicker(season.getEndDate());
        ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        leagueCombo.setConverter(new StringConverter<>() {
            @Override public String toString(League l) { return l == null ? "" : l.getName(); }
            @Override public League fromString(String s) { return null; }
        });
        leagueCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(League item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        leagueCombo.setValue(season.getLeague());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startPicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endPicker, 1, 2);
        grid.add(new Label("League:"), 0, 3);
        grid.add(leagueCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                if (nameField.getText().isBlank() || startPicker.getValue() == null || endPicker.getValue() == null) {
                    showError("Validation", "Name, start date and end date are required");
                    return null;
                }
                season.setName(nameField.getText());
                season.setStartDate(startPicker.getValue());
                season.setEndDate(endPicker.getValue());
                season.setLeague(leagueCombo.getValue());
                return season;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            seasonService.update(season.getId(), updated);
            loadData();
        });
    }

    private void deleteSeason(Season season) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Season");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the season: " + season.getName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                seasonService.deleteById(season.getId());
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

    private void onGenerateSchedule(boolean overwrite) {
        Season selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Generate Schedule", "Please select a season first");
            return;
        }
        if (selected.getLeague() == null) {
            showError("Generate Schedule", "Selected season has no league");
            return;
        }
        var rounds = scheduleService.generateRoundRobin(selected.getLeague(), selected);
        if (rounds.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Schedule");
            a.setHeaderText("No matches to schedule");
            a.setContentText("There are not enough teams to generate a schedule.");
            a.showAndWait();
            return;
        }
        var games = scheduleService.generateAndPersist(selected.getLeague(), selected, overwrite);
        StringBuilder sb = new StringBuilder();
        sb.append("Created ").append(games.size()).append(" games.\n\n");
        rounds.forEach(r -> {
            sb.append("Round ").append(r.getRoundNumber()).append(":\n");
            r.getMatches().forEach(m -> sb.append("  ")
                .append(m.getHome().getName()).append(" vs ")
                .append(m.getAway().getName()).append("\n"));
            sb.append("\n");
        });
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefRowCount(20);
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Generated & Saved Schedule");
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setContent(ta);
        dlg.showAndWait();
        gameView.refresh();
    }

    private void deleteSelected() {
        var selected = table.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showError("Delete Seasons", "Select one or more seasons to delete.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Seasons");
        alert.setHeaderText("Delete " + selected.size() + " seasons?");
        alert.setContentText("This will remove the selected seasons.");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                var toDelete = FXCollections.observableArrayList(selected);
                toDelete.forEach(s -> seasonService.deleteById(s.getId()));
                loadData();
            }
        });
    }
}
