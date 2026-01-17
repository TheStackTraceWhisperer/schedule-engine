package com.scheduleengine.team;

import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.navigation.DrillDownCard;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.league.domain.League;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;

/**
 * Detail view for a specific team with drill-down navigation options
 */
public class TeamDetailView {

    private final TeamService teamService;
    private final LeagueService leagueService;
    private final NavigationHandler navigationHandler;

    public TeamDetailView(TeamService teamService, LeagueService leagueService, NavigationHandler navigationHandler) {
        this.teamService = teamService;
        this.leagueService = leagueService;
        this.navigationHandler = navigationHandler;
    }

    public VBox getView(Team team, NavigationContext currentContext) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ecf0f1;");

        // Header with team info
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label nameLabel = new Label(team.getName());
        nameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox infoBox = new VBox(5);

        if (team.getCoach() != null && !team.getCoach().isBlank()) {
            Label coachLabel = new Label("Coach: " + team.getCoach());
            coachLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            infoBox.getChildren().add(coachLabel);
        }

        if (team.getContactEmail() != null && !team.getContactEmail().isBlank()) {
            Label emailLabel = new Label("Email: " + team.getContactEmail());
            emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            infoBox.getChildren().add(emailLabel);
        }

        if (team.getLeague() != null) {
            Label leagueLabel = new Label("League: " + team.getLeague().getName());
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

        // Roster card
        DrillDownCard rosterCard = new DrillDownCard(
            "View Roster",
            "Browse players on " + team.getName(),
            FontAwesomeIcon.USERS,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "team-roster",
                    "Roster",
                    team
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(rosterCard, Priority.ALWAYS);
        GridPane.setFillWidth(rosterCard, true);

        // Games card
        DrillDownCard gamesCard = new DrillDownCard(
            "View Games",
            "See scheduled and completed games for " + team.getName(),
            FontAwesomeIcon.FUTBOL_ALT,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "team-games",
                    "Games",
                    team
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(gamesCard, Priority.ALWAYS);
        GridPane.setFillWidth(gamesCard, true);

        // Edit team card
        DrillDownCard editCard = new DrillDownCard(
            "Edit Team",
            "Modify team details and information",
            FontAwesomeIcon.EDIT,
            () -> showEditDialog(team, currentContext)
        );
        GridPane.setHgrow(editCard, Priority.ALWAYS);
        GridPane.setFillWidth(editCard, true);

        // Stats card
        DrillDownCard statsCard = new DrillDownCard(
            "View Statistics",
            "See performance and analytics for this team",
            FontAwesomeIcon.BAR_CHART,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "team-stats",
                    "Statistics",
                    team
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(statsCard, Priority.ALWAYS);
        GridPane.setFillWidth(statsCard, true);

        // Payments card
        DrillDownCard paymentsCard = new DrillDownCard(
            "View Payments",
            "See outstanding and pending payments for " + team.getName(),
            FontAwesomeIcon.DOLLAR,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "payments",
                    "Payments",
                    team
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(paymentsCard, Priority.ALWAYS);
        GridPane.setFillWidth(paymentsCard, true);

        // Delete team card
        DrillDownCard deleteCard = new DrillDownCard(
            "Delete Team",
            "Permanently remove this team",
            FontAwesomeIcon.TRASH,
            () -> deleteTeam(team, currentContext)
        );
        deleteCard.setStyle(deleteCard.getStyle() + " -fx-border-color: #dc3545;");
        GridPane.setHgrow(deleteCard, Priority.ALWAYS);
        GridPane.setFillWidth(deleteCard, true);

        cardsGrid.add(rosterCard, 0, 0);
        cardsGrid.add(gamesCard, 1, 0);
        cardsGrid.add(paymentsCard, 2, 0);
        cardsGrid.add(editCard, 0, 1);
        cardsGrid.add(statsCard, 1, 1);
        cardsGrid.add(deleteCard, 2, 1);

        // Configure grid columns to be equal width
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.34);
        cardsGrid.getColumnConstraints().addAll(col1, col2, col3);

        container.getChildren().addAll(header, sectionTitle, cardsGrid);

        return container;
    }

    private void showEditDialog(Team team, NavigationContext currentContext) {
        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle("Edit Team");
        dialog.setHeaderText("Edit team information");

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

        TextField nameField = new TextField(team.getName());
        nameField.setMaxWidth(Double.MAX_VALUE);
        TextField coachField = new TextField(team.getCoach());
        coachField.setMaxWidth(Double.MAX_VALUE);
        TextField emailField = new TextField(team.getContactEmail());
        emailField.setMaxWidth(Double.MAX_VALUE);

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

        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "team.edit", 600, 450));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().isBlank()) {
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

        dialog.showAndWait().ifPresent(updatedTeam -> {
            teamService.update(team.getId(), updatedTeam);
            // Re-navigate to refresh the view with updated data
            Team refreshedTeam = teamService.findById(team.getId())
                .orElse(team);
            NavigationContext newContext = new NavigationContext()
                .navigateTo("teams", "Teams")
                .navigateTo("team-detail", refreshedTeam.getName(), refreshedTeam);
            navigationHandler.navigate(newContext);
        });
    }

    private void deleteTeam(Team team, NavigationContext currentContext) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Team");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the team: " + team.getName() + "?\n\nThis action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                teamService.deleteById(team.getId());
                // Navigate back to teams list
                NavigationContext newContext = new NavigationContext()
                    .navigateTo("teams", "Teams");
                navigationHandler.navigate(newContext);
            }
        });
    }
}

