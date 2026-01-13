package com.scheduleengine.season;

import com.scheduleengine.season.domain.Season;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.navigation.DrillDownCard;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.common.service.ScheduleGeneratorService;
import com.scheduleengine.common.ScheduleGeneratorResultView;
import com.scheduleengine.game.service.GameService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;

/**
 * Detail view for a specific season with drill-down navigation options
 */
public class SeasonDetailView {

    private final SeasonService seasonService;
    private final LeagueService leagueService;
    private final NavigationHandler navigationHandler;
    private final ScheduleGeneratorService scheduleService;
    private final GameService gameService;
    private final ScheduleGeneratorResultView scheduleGeneratorResultView;

    public SeasonDetailView(SeasonService seasonService, LeagueService leagueService, NavigationHandler navigationHandler,
                           ScheduleGeneratorService scheduleService, GameService gameService) {
        this.seasonService = seasonService;
        this.leagueService = leagueService;
        this.navigationHandler = navigationHandler;
        this.scheduleService = scheduleService;
        this.gameService = gameService;
        this.scheduleGeneratorResultView = new ScheduleGeneratorResultView(scheduleService, gameService);
    }

    public VBox getView(Season season, NavigationContext currentContext) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ecf0f1;");

        // Header with season info
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label nameLabel = new Label(season.getName());
        nameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox infoBox = new VBox(5);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String dateRange = season.getStartDate().format(formatter) + " - " +
                          season.getEndDate().format(formatter);
        Label dateLabel = new Label("Dates: " + dateRange);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        infoBox.getChildren().add(dateLabel);

        if (season.getLeague() != null) {
            Label leagueLabel = new Label("League: " + season.getLeague().getName());
            leagueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
            infoBox.getChildren().add(leagueLabel);
        }

        header.getChildren().addAll(nameLabel, infoBox);

        // Section title
        Label sectionTitle = new Label("What would you like to do?");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Drill-down cards
        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);

        // View Games card
        DrillDownCard gamesCard = new DrillDownCard(
            "View Games",
            "See all scheduled games for " + season.getName(),
            FontAwesomeIcon.FUTBOL_ALT,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "season-games",
                    "Games",
                    season
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(gamesCard, Priority.ALWAYS);
        GridPane.setFillWidth(gamesCard, true);

        // View Teams card
        DrillDownCard teamsCard = new DrillDownCard(
            "View Teams",
            "Browse teams participating in this season",
            FontAwesomeIcon.USERS,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "season-teams",
                    "Teams",
                    season
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(teamsCard, Priority.ALWAYS);
        GridPane.setFillWidth(teamsCard, true);

        // Edit season card
        DrillDownCard editCard = new DrillDownCard(
            "Edit Season",
            "Modify season details and dates",
            FontAwesomeIcon.EDIT,
            () -> showEditDialog(season, currentContext)
        );
        GridPane.setHgrow(editCard, Priority.ALWAYS);
        GridPane.setFillWidth(editCard, true);

        // Standings card
        DrillDownCard standingsCard = new DrillDownCard(
            "View Standings",
            "See current standings and rankings",
            FontAwesomeIcon.BAR_CHART,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "season-standings",
                    "Standings",
                    season
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(standingsCard, Priority.ALWAYS);
        GridPane.setFillWidth(standingsCard, true);

        // Generate Schedule card
        DrillDownCard generateCard = new DrillDownCard(
            "Generate Schedule",
            "Create game schedule for this season",
            FontAwesomeIcon.CALENDAR,
            () -> openScheduleGenerator(season, currentContext)
        );
        generateCard.setStyle(generateCard.getStyle() + " -fx-border-color: #43e97b;");
        GridPane.setHgrow(generateCard, Priority.ALWAYS);
        GridPane.setFillWidth(generateCard, true);

        // Delete season card
        DrillDownCard deleteCard = new DrillDownCard(
            "Delete Season",
            "Permanently remove this season",
            FontAwesomeIcon.TRASH,
            () -> deleteSeason(season, currentContext)
        );
        deleteCard.setStyle(deleteCard.getStyle() + " -fx-border-color: #dc3545;");
        GridPane.setHgrow(deleteCard, Priority.ALWAYS);
        GridPane.setFillWidth(deleteCard, true);

        cardsGrid.add(gamesCard, 0, 0);
        cardsGrid.add(teamsCard, 1, 0);
        cardsGrid.add(editCard, 0, 1);
        cardsGrid.add(standingsCard, 1, 1);
        cardsGrid.add(generateCard, 0, 2);
        cardsGrid.add(deleteCard, 1, 2);

        // Configure grid columns to be equal width
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        cardsGrid.getColumnConstraints().addAll(col1, col2);

        container.getChildren().addAll(header, sectionTitle, cardsGrid);

        return container;
    }

    private void showEditDialog(Season season, NavigationContext currentContext) {
        Dialog<Season> dialog = new Dialog<>();
        dialog.setTitle("Edit Season");
        dialog.setHeaderText("Edit season information");

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

        TextField nameField = new TextField(season.getName());
        nameField.setMaxWidth(Double.MAX_VALUE);
        DatePicker startPicker = new DatePicker(season.getStartDate());
        startPicker.setMaxWidth(Double.MAX_VALUE);
        DatePicker endPicker = new DatePicker(season.getEndDate());
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

        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "season.edit", 600, 450));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank() || startPicker.getValue() == null || endPicker.getValue() == null) {
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

        dialog.showAndWait().ifPresent(updatedSeason -> {
            seasonService.update(season.getId(), updatedSeason);
            // Re-navigate to refresh the view with updated data
            Season refreshedSeason = seasonService.findById(season.getId())
                .orElse(season);
            NavigationContext newContext = new NavigationContext()
                .navigateTo("seasons", "Seasons")
                .navigateTo("season-detail", refreshedSeason.getName(), refreshedSeason);
            navigationHandler.navigate(newContext);
        });
    }

    private void openScheduleGenerator(Season season, NavigationContext currentContext) {
        // Create a dialog with the schedule generator view
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Schedule Generator - " + season.getName());
        dialog.setWidth(1000);
        dialog.setHeight(700);

        VBox generatorView = scheduleGeneratorResultView.getView(season);
        dialog.getDialogPane().setContent(generatorView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Make dialog resizable
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "season.schedule.generator", 1000, 700));

        dialog.showAndWait();

        // Refresh the view after schedule generation
        Season refreshedSeason = seasonService.findById(season.getId())
            .orElse(season);
        NavigationContext newContext = new NavigationContext()
            .navigateTo("seasons", "Seasons")
            .navigateTo("season-detail", refreshedSeason.getName(), refreshedSeason);
        navigationHandler.navigate(newContext);
    }

    private void deleteSeason(Season season, NavigationContext currentContext) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Season");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the season: " + season.getName() + "?\n\nThis action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                seasonService.deleteById(season.getId());
                // Navigate back to seasons list
                NavigationContext newContext = new NavigationContext()
                    .navigateTo("seasons", "Seasons");
                navigationHandler.navigate(newContext);
            }
        });
    }
}

