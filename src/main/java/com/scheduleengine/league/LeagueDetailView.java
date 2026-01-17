package com.scheduleengine.league;

import com.scheduleengine.common.DialogUtil;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.navigation.DrillDownCard;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Detail view for a specific league with drill-down navigation options
 */
public class LeagueDetailView {

  private final LeagueService leagueService;
  private final NavigationHandler navigationHandler;
  private League league;

  public LeagueDetailView(LeagueService leagueService, NavigationHandler navigationHandler) {
    this.leagueService = leagueService;
    this.navigationHandler = navigationHandler;
  }

  public VBox getView(League league, NavigationContext currentContext) {
    this.league = league;

    VBox container = new VBox(20);
    container.setPadding(new Insets(20));
    container.setStyle("-fx-background-color: #ecf0f1;");

    // Header with league info
    VBox header = new VBox(10);
    header.setPadding(new Insets(30));
    header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

    Label nameLabel = new Label(league.getName());
    nameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

    if (league.getDescription() != null && !league.getDescription().isBlank()) {
      Label descLabel = new Label(league.getDescription());
      descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
      descLabel.setWrapText(true);
      header.getChildren().addAll(nameLabel, descLabel);
    } else {
      header.getChildren().add(nameLabel);
    }

    // Section title
    Label sectionTitle = new Label("What would you like to do?");
    sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

    // Drill-down cards
    GridPane cardsGrid = new GridPane();
    cardsGrid.setHgap(20);
    cardsGrid.setVgap(20);
    // Configure grid columns to be equal width BEFORE adding or accessing constraints
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPercentWidth(50);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setPercentWidth(50);
    cardsGrid.getColumnConstraints().addAll(col1, col2);

    // Teams card
    DrillDownCard teamsCard = new DrillDownCard(
      "View Teams",
      "Browse all teams in " + league.getName(),
      FontAwesomeIcon.USERS,
      () -> {
        NavigationContext newContext = currentContext.navigateTo(
          "league-teams",
          "Teams",
          league
        );
        navigationHandler.navigate(newContext);
      }
    );
    GridPane.setHgrow(teamsCard, Priority.ALWAYS);
    GridPane.setFillWidth(teamsCard, true);

    // Seasons card
    DrillDownCard seasonsCard = new DrillDownCard(
      "View Seasons",
      "Browse all seasons in " + league.getName(),
      FontAwesomeIcon.CALENDAR,
      () -> {
        NavigationContext newContext = currentContext.navigateTo(
          "league-seasons",
          "Seasons",
          league
        );
        navigationHandler.navigate(newContext);
      }
    );
    GridPane.setHgrow(seasonsCard, Priority.ALWAYS);
    GridPane.setFillWidth(seasonsCard, true);

    // Edit league card
    DrillDownCard editCard = new DrillDownCard(
      "Edit League",
      "Modify league details and settings",
      FontAwesomeIcon.EDIT,
      () -> showEditDialog(league, currentContext)
    );
    GridPane.setHgrow(editCard, Priority.ALWAYS);
    GridPane.setFillWidth(editCard, true);

    // Stats card
    DrillDownCard statsCard = new DrillDownCard(
      "View Statistics",
      "See analytics and reports for this league",
      FontAwesomeIcon.BAR_CHART,
      () -> {
        NavigationContext newContext = currentContext.navigateTo(
          "league-stats",
          "Statistics",
          league
        );
        navigationHandler.navigate(newContext);
      }
    );
    GridPane.setHgrow(statsCard, Priority.ALWAYS);
    GridPane.setFillWidth(statsCard, true);

    // Payments card
    DrillDownCard paymentsCard = new DrillDownCard(
      "View Payment Details",
      "Open payments for due invoices in this league",
      FontAwesomeIcon.MONEY,
      () -> {
        NavigationContext newContext = currentContext.navigateTo(
          "payments",
          "Payments",
          league
        );
        navigationHandler.navigate(newContext);
      }
    );
    GridPane.setHgrow(paymentsCard, Priority.ALWAYS);
    GridPane.setFillWidth(paymentsCard, true);

    // Delete league card
    DrillDownCard deleteCard = new DrillDownCard(
      "Delete League",
      "Permanently remove this league",
      FontAwesomeIcon.TRASH,
      () -> deleteLeague(league, currentContext)
    );
    deleteCard.setStyle(deleteCard.getStyle() + " -fx-border-color: #dc3545;");
    GridPane.setHgrow(deleteCard, Priority.ALWAYS);
    GridPane.setFillWidth(deleteCard, true);

    cardsGrid.add(teamsCard, 0, 0);
    cardsGrid.add(seasonsCard, 1, 0);
    cardsGrid.add(editCard, 0, 1);
    cardsGrid.add(statsCard, 1, 1);
    cardsGrid.add(paymentsCard, 1, 2);
    cardsGrid.add(deleteCard, 0, 2);

    container.getChildren().addAll(header, sectionTitle, cardsGrid);

    return container;
  }

  private void showEditDialog(League league, NavigationContext currentContext) {
    Dialog<League> dialog = new Dialog<>();
    dialog.setTitle("Edit League");
    dialog.setHeaderText("Edit league information");

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

    TextField nameField = new TextField(league.getName());
    nameField.setMaxWidth(Double.MAX_VALUE);
    TextArea descField = new TextArea(league.getDescription());
    descField.setPrefRowCount(3);
    descField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setVgrow(descField, Priority.ALWAYS);

    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Description:"), 0, 1);
    grid.add(descField, 1, 1);

    dialog.getDialogPane().setContent(grid);

    // Make dialog resizable and persist size
    dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
      DialogUtil.makeResizable(dialog, "league.edit", 550, 400));

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == saveButtonType) {
        league.setName(nameField.getText());
        league.setDescription(descField.getText());
        return league;
      }
      return null;
    });

    dialog.showAndWait().ifPresent(updatedLeague -> {
      leagueService.update(league.getId(), updatedLeague);
      // Re-navigate to refresh the view with updated data
      League refreshedLeague = leagueService.findById(league.getId())
        .orElse(league);
      NavigationContext newContext = new NavigationContext()
        .navigateTo("leagues", "Leagues")
        .navigateTo("league-detail", refreshedLeague.getName(), refreshedLeague);
      navigationHandler.navigate(newContext);
    });
  }

  private void deleteLeague(League league, NavigationContext currentContext) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete League");
    alert.setHeaderText("Are you sure?");
    alert.setContentText("Do you want to delete the league: " + league.getName() + "?\n\nThis action cannot be undone.");

    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        leagueService.deleteById(league.getId());
        // Navigate back to leagues list
        NavigationContext newContext = new NavigationContext()
          .navigateTo("leagues", "Leagues");
        navigationHandler.navigate(newContext);
      }
    });
  }
}

