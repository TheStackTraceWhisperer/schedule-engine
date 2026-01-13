package com.scheduleengine.season;

import com.scheduleengine.league.domain.League;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.game.GameView;
import com.scheduleengine.common.service.ScheduleGeneratorService;
import com.scheduleengine.common.ScheduleGeneratorResultView;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.common.DialogUtil;
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

public class SeasonView {
    
    private final SeasonService seasonService;
    private final LeagueService leagueService;
    private TableView<Season> table;
    private ObservableList<Season> data;
    private ScheduleGeneratorService scheduleService;
    private final GameView gameView;
    private final GameService gameService;
    private ScheduleGeneratorResultView scheduleGeneratorResultView;
    private League filterLeague; // League to filter by, if any
    private com.scheduleengine.navigation.NavigationHandler navigationHandler;

    public SeasonView(SeasonService seasonService, LeagueService leagueService, ScheduleGeneratorService scheduleService, GameView gameView, GameService gameService) {
        this.seasonService = seasonService;
        this.leagueService = leagueService;
        this.scheduleService = scheduleService;
        this.gameView = gameView;
        this.gameService = gameService;
        this.data = FXCollections.observableArrayList();
        this.scheduleGeneratorResultView = new ScheduleGeneratorResultView(scheduleService, gameService);
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
        Label title = new Label(filterLeague != null ?
            "Seasons - " + filterLeague.getName() : "Seasons");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add Season");
        addButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddDialog());

        topBox.getChildren().addAll(title, spacer, addButton);

        table = new TableView<>();
        table.setItems(data);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<Season, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setVisible(false);

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
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white;");
                viewBtn.setOnAction(e -> {
                    Season season = getTableView().getItems().get(getIndex());
                    if (navigationHandler != null) {
                        com.scheduleengine.navigation.NavigationContext newContext =
                            new com.scheduleengine.navigation.NavigationContext()
                                .navigateTo("seasons", "Seasons")
                                .navigateTo("season-detail", season.getName(), season);
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
                    HBox box = new HBox(6, viewBtn);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, startCol, endCol, leagueCol, actionCol);

        // Setup column width persistence
        TablePreferencesUtil.setupTableColumnPersistence(table, "season.table");
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
        if (filterLeague != null) {
            // Filter seasons by league
            data.addAll(seasonService.findAll().stream()
                .filter(season -> season.getLeague() != null &&
                        season.getLeague().getId().equals(filterLeague.getId()))
                .toList());
        } else {
            data.addAll(seasonService.findAll());
        }
    }

    /**
     * Set the league to filter seasons by
     */
    public void setFilterLeague(League league) {
        this.filterLeague = league;
    }

    /**
     * Clear any league filter
     */
    public void clearFilter() {
        this.filterLeague = null;
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

        // Configure columns: label column fixed, field column grows
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(Region.USE_PREF_SIZE);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setMinWidth(350);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        TextField nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);
        DatePicker startPicker = new DatePicker();
        startPicker.setMaxWidth(Double.MAX_VALUE);
        DatePicker endPicker = new DatePicker();
        endPicker.setMaxWidth(Double.MAX_VALUE);
        ComboBox<League> leagueCombo = new ComboBox<>(FXCollections.observableArrayList(leagueService.findAll()));
        leagueCombo.setMaxWidth(Double.MAX_VALUE);
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

        // Auto-fill league if view is filtered by a league
        if (filterLeague != null) {
            leagueCombo.setValue(filterLeague);
            // Auto-generate season name: "League Name - Season/Year"
            nameField.setText(generateSeasonName(filterLeague));
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startPicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endPicker, 1, 2);
        grid.add(new Label("League:"), 0, 3);
        grid.add(leagueCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "season.add", 600, 450));

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
            try {
                seasonService.save(season);
                loadData();
            } catch (IllegalArgumentException ex) {
                showError("Duplicate Season Name", ex.getMessage());
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                showError("Duplicate Season Name",
                    "A season with the name '" + season.getName() + "' already exists. Please choose a different name.");
            } catch (Exception ex) {
                showError("Error", "Failed to save season: " + ex.getMessage());
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


    /**
     * Generate a season name based on league name and current date.
     * Format: "League Name - Season/Year"
     * Examples: "Premier League - Spring/2026", "Youth League - Fall/2025"
     */
    private String generateSeasonName(League league) {
        if (league == null) {
            return "";
        }

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        String season;

        // Determine season based on current month
        int month = now.getMonthValue();
        if (month >= 3 && month <= 5) {
            season = "Spring";
        } else if (month >= 6 && month <= 8) {
            season = "Summer";
        } else if (month >= 9 && month <= 11) {
            season = "Fall";
        } else {
            // Winter: December, January, February
            season = "Winter";
        }

        return league.getName() + " - " + season + "/" + year;
    }
}

