package com.scheduleengine.game;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.game.domain.Game;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.common.TablePreferencesUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class GameView {
    
    private final GameService gameService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final LeagueService leagueService;
    private TableView<Game> table;
    private ObservableList<Game> data;
    private ComboBox<Season> seasonFilter;
    private ComboBox<League> leagueFilter;
    private Team filterTeam; // Team to filter by, if any
    private Season filterSeason; // Season to filter by, if any
    private League filterLeague; // League to filter by, if any
    private com.scheduleengine.navigation.NavigationHandler navigationHandler;

    public GameView(GameService gameService, TeamService teamService,
                   FieldService fieldService, SeasonService seasonService, LeagueService leagueService) {
        this.gameService = gameService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.leagueService = leagueService;
        this.data = FXCollections.observableArrayList();
    }
    
    /**
     * Set the navigation handler for drill-down navigation
     */
    public void setNavigationHandler(com.scheduleengine.navigation.NavigationHandler navigationHandler) {
        this.navigationHandler = navigationHandler;
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        
        HBox topBox = new HBox(10);
        // Build title showing active filter
        String titleText = "Games";
        if (filterTeam != null) {
            titleText = "Games - " + filterTeam.getName();
        } else if (filterSeason != null) {
            titleText = "Games - " + filterSeason.getName();
        } else if (filterLeague != null) {
            titleText = "Games - " + filterLeague.getName();
        }
        Label title = new Label(titleText);
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
        leagueFilter.valueProperty().addListener((obs, o, n) -> loadData());

        // Pre-select league if filter is set
        if (filterLeague != null) {
            leagueFilter.setValue(filterLeague);
        }

        seasonFilter = new ComboBox<>(FXCollections.observableArrayList(seasonService.findAll()));
        seasonFilter.setPromptText("Filter by Season");
        seasonFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Season s) { return s == null ? "" : s.getName(); }
            @Override public Season fromString(String s) { return null; }
        });
        seasonFilter.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Season item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        seasonFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadData());

        // Pre-select season if filter is set
        if (filterSeason != null) {
            seasonFilter.setValue(filterSeason);
        }

        Button clearFilter = new Button("Clear");
        clearFilter.setOnAction(e -> { seasonFilter.setValue(null); leagueFilter.setValue(null); loadData(); });

        Button addButton = new Button("Add Game");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());

        // Add "Back to Team Detail" button if filtered by team
        if (filterTeam != null && navigationHandler != null) {
            Button backToTeamBtn = new Button("← Back to " + filterTeam.getName());
            backToTeamBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
            backToTeamBtn.setOnAction(e -> {
                com.scheduleengine.navigation.NavigationContext newContext =
                    new com.scheduleengine.navigation.NavigationContext()
                        .navigateTo("teams", "Teams")
                        .navigateTo("team-detail", filterTeam.getName(), filterTeam);
                navigationHandler.navigate(newContext);
            });

            topBox.getChildren().addAll(title, spacer, backToTeamBtn,
                new Label("League:"), leagueFilter,
                new Label("Season:"), seasonFilter, clearFilter, addButton);
        } else {
            topBox.getChildren().addAll(title, spacer,
                new Label("League:"), leagueFilter,
                new Label("Season:"), seasonFilter, clearFilter, addButton);
        }

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Game, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setVisible(false);

        TableColumn<Game, LocalDateTime> dateCol = new TableColumn<>("Date/Time");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("gameDate"));
        dateCol.setPrefWidth(180);

        TableColumn<Game, String> homeCol = new TableColumn<>("Home Team");
        homeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            c.getValue().getHomeTeam() != null ? c.getValue().getHomeTeam().getName() : ""
        ));
        homeCol.setPrefWidth(160);

        TableColumn<Game, String> awayCol = new TableColumn<>("Away Team");
        awayCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            c.getValue().getAwayTeam() != null ? c.getValue().getAwayTeam().getName() : ""
        ));
        awayCol.setPrefWidth(160);

        TableColumn<Game, String> fieldCol = new TableColumn<>("Field");
        fieldCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            c.getValue().getField() != null ? c.getValue().getField().getName() : ""
        ));
        fieldCol.setPrefWidth(160);

        TableColumn<Game, String> seasonCol = new TableColumn<>("Season");
        seasonCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            c.getValue().getSeason() != null ? c.getValue().getSeason().getName() : ""
        ));
        seasonCol.setPrefWidth(160);

        TableColumn<Game, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Game, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
            (c.getValue().getHomeScore() != null ? c.getValue().getHomeScore() : 0) +
            " - " + (c.getValue().getAwayScore() != null ? c.getValue().getAwayScore() : 0)
        ));
        scoreCol.setPrefWidth(100);

        TableColumn<Game, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(160);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    Game game = getTableView().getItems().get(getIndex());
                    if (navigationHandler != null) {
                        com.scheduleengine.navigation.NavigationContext newContext =
                            new com.scheduleengine.navigation.NavigationContext()
                                .navigateTo("games", "Games")
                                .navigateTo("game-detail", "Game #" + game.getId(), game);
                        navigationHandler.navigate(newContext);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, viewBtn));
            }
        });

        table.getColumns().addAll(idCol, dateCol, homeCol, awayCol, fieldCol, seasonCol, statusCol, scoreCol, actionCol);

        // Setup column width persistence (unique table id)
        TablePreferencesUtil.setupTableColumnPersistence(table, "game.table");

        loadData();
        
        vbox.getChildren().addAll(topBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return vbox;
    }
    
    public void refresh() {
        loadData();
    }

    /**
     * Set the team to filter games by
     */
    public void setFilterTeam(Team team) {
        this.filterTeam = team;
    }

    /**
     * Set the season to filter games by
     */
    public void setFilterSeason(Season season) {
        this.filterSeason = season;
    }

    /**
     * Set the league to filter games by
     */
    public void setFilterLeague(League league) {
        this.filterLeague = league;
    }

    /**
     * Clear all filters
     */
    public void clearFilter() {
        this.filterTeam = null;
        this.filterSeason = null;
        this.filterLeague = null;
    }

    private void loadData() {
        data.clear();
        // Use combo box values if set, otherwise use preset filters
        Season selectedSeason = seasonFilter != null ? seasonFilter.getValue() : filterSeason;
        League selectedLeague = leagueFilter != null ? leagueFilter.getValue() : filterLeague;
        var list = selectedSeason != null ? gameService.findBySeasonId(selectedSeason.getId()) : gameService.findAll();

        // Filter by league
        if (selectedLeague != null) {
            list = list.stream().filter(g -> {
                if (g.getSeason() != null && g.getSeason().getLeague() != null && selectedLeague.getId().equals(g.getSeason().getLeague().getId())) {
                    return true;
                }
                if (g.getHomeTeam() != null && g.getHomeTeam().getLeague() != null && selectedLeague.getId().equals(g.getHomeTeam().getLeague().getId())) {
                    return true;
                }
                if (g.getAwayTeam() != null && g.getAwayTeam().getLeague() != null && selectedLeague.getId().equals(g.getAwayTeam().getLeague().getId())) {
                    return true;
                }
                return false;
            }).toList();
        }

        // Filter by team (if navigating from team detail)
        if (filterTeam != null) {
            list = list.stream().filter(g -> {
                boolean isHomeTeam = g.getHomeTeam() != null && filterTeam.getId().equals(g.getHomeTeam().getId());
                boolean isAwayTeam = g.getAwayTeam() != null && filterTeam.getId().equals(g.getAwayTeam().getId());
                return isHomeTeam || isAwayTeam;
            }).toList();
        }

        data.addAll(list);
    }
    
    private void showAddDialog() {
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Add Game");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = buildGameForm(null);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                return buildGameFromForm(grid, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(game -> {
            gameService.save(game);
            loadData();
        });
    }

    private void showEditDialog(Game game) {
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Edit Game");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = buildGameForm(game);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                return buildGameFromForm(grid, game);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            gameService.update(game.getId(), updated);
            loadData();
        });
    }

    public GridPane buildGameForm(Game existing) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker datePicker = new DatePicker();
        Spinner<Integer> hour = new Spinner<>(0, 23, 18);
        Spinner<Integer> minute = new Spinner<>(0, 59, 0, 5);

        ComboBox<Team> homeCombo = new ComboBox<>(FXCollections.observableArrayList(teamService.findAll()));
        ComboBox<Team> awayCombo = new ComboBox<>(FXCollections.observableArrayList(teamService.findAll()));
        ComboBox<Field> fieldCombo = new ComboBox<>(FXCollections.observableArrayList(fieldService.findAll()));
        ComboBox<Season> seasonCombo = new ComboBox<>(FXCollections.observableArrayList(seasonService.findAll()));

        StringConverter<Team> teamConv = new StringConverter<>() {
            @Override public String toString(Team t) { return t == null ? "" : t.getName(); }
            @Override public Team fromString(String s) { return null; }
        };
        homeCombo.setConverter(teamConv); awayCombo.setConverter(teamConv);
        homeCombo.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Team item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item.getName()); }});
        awayCombo.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Team item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item.getName()); }});

        fieldCombo.setConverter(new StringConverter<>() { @Override public String toString(Field f){return f==null?"":f.getName();} @Override public Field fromString(String s){return null;} });
        fieldCombo.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Field item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item.getName()); }});
        seasonCombo.setConverter(new StringConverter<>() { @Override public String toString(Season s){return s==null?"":s.getName();} @Override public Season fromString(String s){return null;} });
        seasonCombo.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Season item, boolean empty){ super.updateItem(item, empty); setText(empty||item==null?null:item.getName()); }});

        Spinner<Integer> homeScore = new Spinner<>(0, 99, 0);
        Spinner<Integer> awayScore = new Spinner<>(0, 99, 0);
        ComboBox<Game.GameStatus> statusCombo = new ComboBox<>(FXCollections.observableArrayList(Game.GameStatus.values()));

        if (existing != null) {
            LocalDateTime dt = existing.getGameDate();
            datePicker.setValue(dt.toLocalDate());
            hour.getValueFactory().setValue(dt.getHour());
            minute.getValueFactory().setValue(dt.getMinute());
            homeCombo.setValue(existing.getHomeTeam());
            awayCombo.setValue(existing.getAwayTeam());
            fieldCombo.setValue(existing.getField());
            seasonCombo.setValue(existing.getSeason());
            homeScore.getValueFactory().setValue(existing.getHomeScore() != null ? existing.getHomeScore() : 0);
            awayScore.getValueFactory().setValue(existing.getAwayScore() != null ? existing.getAwayScore() : 0);
            statusCombo.setValue(existing.getStatus());
        }

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time (HH:MM):"), 0, 1);
        HBox timeBox = new HBox(8, hour, new Label(":"), minute);
        grid.add(timeBox, 1, 1);
        grid.add(new Label("Home Team:"), 0, 2);
        grid.add(homeCombo, 1, 2);
        grid.add(new Label("Away Team:"), 0, 3);
        grid.add(awayCombo, 1, 3);
        grid.add(new Label("Field:"), 0, 4);
        grid.add(fieldCombo, 1, 4);
        grid.add(new Label("Season:"), 0, 5);
        grid.add(seasonCombo, 1, 5);
        grid.add(new Label("Home Score:"), 0, 6);
        grid.add(homeScore, 1, 6);
        grid.add(new Label("Away Score:"), 0, 7);
        grid.add(awayScore, 1, 7);
        grid.add(new Label("Status:"), 0, 8);
        grid.add(statusCombo, 1, 8);

        return grid;
    }

    public Game buildGameFromForm(GridPane grid, Game base) {
        // Retrieve nodes by position reliably
        DatePicker dp = (DatePicker) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) != null && GridPane.getColumnIndex(n) != null && GridPane.getRowIndex(n) == 0 && GridPane.getColumnIndex(n) == 1).get(0);
        HBox timeBox = (HBox) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 1 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        Spinner<Integer> hour = (Spinner<Integer>) timeBox.getChildren().get(0);
        @SuppressWarnings("unchecked")
        Spinner<Integer> minute = (Spinner<Integer>) timeBox.getChildren().get(2);
        @SuppressWarnings("unchecked")
        ComboBox<Team> homeCombo = (ComboBox<Team>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 2 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        ComboBox<Team> awayCombo = (ComboBox<Team>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 3 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        ComboBox<Field> fieldCombo = (ComboBox<Field>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 4 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        ComboBox<Season> seasonCombo = (ComboBox<Season>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 5 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        Spinner<Integer> homeScore = (Spinner<Integer>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 6 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        Spinner<Integer> awayScore = (Spinner<Integer>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 7 && GridPane.getColumnIndex(n) == 1).get(0);
        @SuppressWarnings("unchecked")
        ComboBox<Game.GameStatus> statusCombo = (ComboBox<Game.GameStatus>) grid.getChildren().filtered(n -> GridPane.getRowIndex(n) == 8 && GridPane.getColumnIndex(n) == 1).get(0);

        if (dp.getValue() == null || homeCombo.getValue() == null || awayCombo.getValue() == null) {
            showError("Validation", "Date, home team and away team are required");
            return null;
        }
        LocalDate d = dp.getValue();
        Integer h = hour.getValue();
        Integer m = minute.getValue();
        LocalDateTime dt = LocalDateTime.of(d, LocalTime.of(h, m));

        Game g = base == null ? new Game() : base;
        g.setGameDate(dt);
        g.setHomeTeam(homeCombo.getValue());
        g.setAwayTeam(awayCombo.getValue());
        g.setField(fieldCombo.getValue());
        g.setSeason(seasonCombo.getValue());
        g.setHomeScore(homeScore.getValue());
        g.setAwayScore(awayScore.getValue());
        g.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : Game.GameStatus.SCHEDULED);
        return g;
    }

    private void deleteGame(Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Game");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete game #" + game.getId() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                gameService.deleteById(game.getId());
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
