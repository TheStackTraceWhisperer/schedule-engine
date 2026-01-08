package com.scheduleengine;

import com.scheduleengine.domain.League;
import com.scheduleengine.domain.Team;
import com.scheduleengine.service.LeagueService;
import com.scheduleengine.service.TeamService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TeamView {

    private final TeamService teamService;
    private final LeagueService leagueService;
    private TableView<Team> table;
    private ObservableList<Team> data;
    private ComboBox<League> leagueFilter;

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

        leagueFilter = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        leagueFilter.setPromptText("Filter by League");
        leagueFilter.setConverter(new StringConverter<>() {
            @Override public String toString(League l) { return l == null ? "" : l.getName(); }
            @Override public League fromString(String s) { return null; }
        });
        leagueFilter.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(League item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        leagueFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

        Button clearFilter = new Button("Clear");
        clearFilter.setOnAction(e -> { leagueFilter.setValue(null); loadData(); });

        Button addButton = new Button("Add Team");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData());

        Button deleteSelected = new Button("Delete Selected");
        deleteSelected.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        deleteSelected.setOnAction(e -> deleteSelected());

        topBox.getChildren().addAll(title, spacer, new Label("League:"), leagueFilter, clearFilter, refreshButton, addButton, deleteSelected);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Team, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Team, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Team, String> coachCol = new TableColumn<>("Coach");
        coachCol.setCellValueFactory(new PropertyValueFactory<>("coach"));
        coachCol.setPrefWidth(140);

        TableColumn<Team, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        emailCol.setPrefWidth(220);

        TableColumn<Team, String> leagueCol = new TableColumn<>("League");
        leagueCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
            cell.getValue().getLeague() != null ? cell.getValue().getLeague().getName() : ""
        ));
        leagueCol.setPrefWidth(180);

        TableColumn<Team, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            {
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteTeam(getTableView().getItems().get(getIndex())));
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

        table.getColumns().addAll(idCol, nameCol, coachCol, emailCol, leagueCol, actionCol);

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
        League selected = leagueFilter != null ? leagueFilter.getValue() : null;
        if (selected != null) {
            data.addAll(teamService.findByLeagueId(selected.getId()));
        } else {
            data.addAll(teamService.findAll());
        }
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
        ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        configureLeagueCombo(leagueCombo);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Coach:"), 0, 1);
        grid.add(coachField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("League:"), 0, 3);
        grid.add(leagueCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
                    showError("Validation", "Name is required");
                    return null;
                }
                Team team = new Team();
                team.setName(nameField.getText());
                team.setCoach(coachField.getText());
                team.setContactEmail(emailField.getText());
                team.setLeague(leagueCombo.getValue());
                return team;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(team -> {
            teamService.save(team);
            loadData();
        });
    }

    private void showEditDialog(Team team) {
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle("Edit Team");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(team.getName());
        TextField coachField = new TextField(team.getCoach());
        TextField emailField = new TextField(team.getContactEmail());
        ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        configureLeagueCombo(leagueCombo);
        leagueCombo.setValue(team.getLeague());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Coach:"), 0, 1);
        grid.add(coachField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("League:"), 0, 3);
        grid.add(leagueCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
                    showError("Validation", "Name is required");
                    return null;
                }
                team.setName(nameField.getText());
                team.setCoach(coachField.getText());
                team.setContactEmail(emailField.getText());
                team.setLeague(leagueCombo.getValue());
                return team;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            teamService.update(team.getId(), updated);
            loadData();
        });
    }

    private void deleteTeam(Team team) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Team");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the team: " + team.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                teamService.deleteById(team.getId());
                loadData();
            }
        });
    }

    private void deleteSelected() {
        var selected = table.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showError("Delete Teams", "Select one or more teams to delete.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Teams");
        alert.setHeaderText("Delete " + selected.size() + " teams?");
        alert.setContentText("This will remove the selected teams.");
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                // Copy to avoid concurrent modification
                var toDelete = FXCollections.observableArrayList(selected);
                toDelete.forEach(t -> teamService.deleteById(t.getId()));
                loadData();
            }
        });
    }

    private void configureLeagueCombo(ComboBox<League> combo) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(League object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public League fromString(String string) { return null; }
        });
        Callback<ListView<League>, ListCell<League>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(League item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        };
        combo.setCellFactory(cellFactory);
        combo.setButtonCell(cellFactory.call(null));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
