package com.scheduleengine.player;

import com.scheduleengine.player.domain.Player;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.team.service.TeamService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

public class RosterView {

    private final PlayerService playerService;
    private final TeamService teamService;
    private TableView<Player> table;
    private ObservableList<Player> data;
    private ComboBox<Team> teamFilter;
    private Team selectedTeam;

    public RosterView(PlayerService playerService, TeamService teamService) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.data = FXCollections.observableArrayList();
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        HBox topBox = new HBox(10);
        Label title = new Label("Team Rosters");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        teamFilter = new ComboBox<>(FXCollections.observableArrayList(teamService.findAll()));
        teamFilter.setPromptText("Select a Team");
        teamFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Team t) { return t == null ? "" : t.getName(); }
            @Override public Team fromString(String s) { return null; }
        });
        teamFilter.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Team item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        teamFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedTeam = newVal;
            loadData();
        });

        Button addButton = new Button("Add Player");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> {
            if (selectedTeam == null) {
                showError("Select Team", "Please select a team first.");
                return;
            }
            showAddDialog();
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());

        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteSelected.setOnAction(e -> deleteSelected());

        topBox.getChildren().addAll(title, spacer, new Label("Team:"), teamFilter, refreshButton, addButton, deleteSelected);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Player, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Player, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(140);

        TableColumn<Player, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(140);

        TableColumn<Player, Integer> jerseyCol = new TableColumn<>("Jersey #");
        jerseyCol.setCellValueFactory(new PropertyValueFactory<>("jerseyNumber"));
        jerseyCol.setPrefWidth(100);

        TableColumn<Player, String> positionCol = new TableColumn<>("Position");
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        positionCol.setPrefWidth(140);

        TableColumn<Player, String> teamCol = new TableColumn<>("Team");
        teamCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            c.getValue().getTeam() != null ? c.getValue().getTeam().getName() : ""
        ));
        teamCol.setPrefWidth(140);

        TableColumn<Player, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deletePlayer(getTableView().getItems().get(getIndex())));
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

        table.getColumns().addAll(idCol, firstNameCol, lastNameCol, jerseyCol, positionCol, teamCol, actionCol);

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
        if (selectedTeam != null) {
            data.addAll(playerService.findByTeamId(selectedTeam.getId()));
        }
    }

    private void showAddDialog() {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle("Add Player");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        Spinner<Integer> jerseySpinner = new Spinner<>(0, 99, 0);
        TextField positionField = new TextField();
        positionField.setPromptText("Position (e.g., Forward, Midfielder)");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Jersey Number:"), 0, 2);
        grid.add(jerseySpinner, 1, 2);
        grid.add(new Label("Position:"), 0, 3);
        grid.add(positionField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
                    showError("Validation", "First and last names are required.");
                    return null;
                }
                Player player = new Player();
                player.setFirstName(firstNameField.getText());
                player.setLastName(lastNameField.getText());
                player.setJerseyNumber(jerseySpinner.getValue());
                player.setPosition(positionField.getText());
                player.setTeam(selectedTeam);
                return player;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(player -> {
            playerService.save(player);
            loadData();
        });
    }

    private void showEditDialog(Player player) {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle("Edit Player");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField(player.getFirstName());
        TextField lastNameField = new TextField(player.getLastName());
        Spinner<Integer> jerseySpinner = new Spinner<>(0, 99, player.getJerseyNumber() != null ? player.getJerseyNumber() : 0);
        TextField positionField = new TextField(player.getPosition() != null ? player.getPosition() : "");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Jersey Number:"), 0, 2);
        grid.add(jerseySpinner, 1, 2);
        grid.add(new Label("Position:"), 0, 3);
        grid.add(positionField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
                    showError("Validation", "First and last names are required.");
                    return null;
                }
                player.setFirstName(firstNameField.getText());
                player.setLastName(lastNameField.getText());
                player.setJerseyNumber(jerseySpinner.getValue());
                player.setPosition(positionField.getText());
                return player;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            playerService.update(player.getId(), updated);
            loadData();
        });
    }

    private void deletePlayer(Player player) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Player");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete " + player.getFullName() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                playerService.deleteById(player.getId());
                loadData();
            }
        });
    }

    private void deleteSelected() {
        var selected = table.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showError("Delete Players", "Select one or more players to delete.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Players");
        alert.setHeaderText("Delete " + selected.size() + " players?");
        alert.setContentText("This will remove the selected players from the roster.");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                var toDelete = FXCollections.observableArrayList(selected);
                toDelete.forEach(p -> playerService.deleteById(p.getId()));
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

