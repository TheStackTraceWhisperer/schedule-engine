package com.scheduleengine.player;

import com.scheduleengine.player.domain.Player;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.navigation.DrillDownCard;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import com.scheduleengine.common.DialogUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;

/**
 * Detail view for a specific player with management options
 */
public class PlayerDetailView {

    private final PlayerService playerService;
    private final TeamService teamService;
    private final NavigationHandler navigationHandler;

    public PlayerDetailView(PlayerService playerService, TeamService teamService, NavigationHandler navigationHandler) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.navigationHandler = navigationHandler;
    }

    public VBox getView(Player player, NavigationContext currentContext) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ecf0f1;");

        // Header with player info
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label nameLabel = new Label(player.getFullName());
        nameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox infoBox = new VBox(5);

        if (player.getJerseyNumber() != null) {
            Label jerseyLabel = new Label("Jersey #" + player.getJerseyNumber());
            jerseyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            infoBox.getChildren().add(jerseyLabel);
        }

        if (player.getPosition() != null && !player.getPosition().isBlank()) {
            Label positionLabel = new Label("Position: " + player.getPosition());
            positionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            infoBox.getChildren().add(positionLabel);
        }

        if (player.getTeam() != null) {
            Label teamLabel = new Label("Team: " + player.getTeam().getName());
            teamLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
            infoBox.getChildren().add(teamLabel);
        }

        header.getChildren().addAll(nameLabel, infoBox);

        // Section title
        Label sectionTitle = new Label("What would you like to do?");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Drill-down cards
        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);

        // Edit player card
        DrillDownCard editCard = new DrillDownCard(
            "Edit Player",
            "Modify player details and information",
            FontAwesomeIcon.EDIT,
            () -> showEditDialog(player, currentContext)
        );
        GridPane.setHgrow(editCard, Priority.ALWAYS);
        GridPane.setFillWidth(editCard, true);

        // View stats card (placeholder for future)
        DrillDownCard statsCard = new DrillDownCard(
            "View Statistics",
            "See performance and game statistics",
            FontAwesomeIcon.BAR_CHART,
            () -> {
                NavigationContext newContext = currentContext.navigateTo(
                    "player-stats",
                    "Statistics",
                    player
                );
                navigationHandler.navigate(newContext);
            }
        );
        GridPane.setHgrow(statsCard, Priority.ALWAYS);
        GridPane.setFillWidth(statsCard, true);

        // Delete player card
        DrillDownCard deleteCard = new DrillDownCard(
            "Delete Player",
            "Permanently remove this player",
            FontAwesomeIcon.TRASH,
            () -> deletePlayer(player, currentContext)
        );
        deleteCard.setStyle(deleteCard.getStyle() + " -fx-border-color: #dc3545;");
        GridPane.setHgrow(deleteCard, Priority.ALWAYS);
        GridPane.setFillWidth(deleteCard, true);

        cardsGrid.add(editCard, 0, 0);
        cardsGrid.add(statsCard, 1, 0);
        cardsGrid.add(deleteCard, 0, 1);

        // Configure grid columns to be equal width
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        cardsGrid.getColumnConstraints().addAll(col1, col2);

        container.getChildren().addAll(header, sectionTitle, cardsGrid);

        return container;
    }

    private void showEditDialog(Player player, NavigationContext currentContext) {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle("Edit Player");
        dialog.setHeaderText("Edit player information");

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

        TextField firstNameField = new TextField(player.getFirstName());
        firstNameField.setMaxWidth(Double.MAX_VALUE);
        TextField lastNameField = new TextField(player.getLastName());
        lastNameField.setMaxWidth(Double.MAX_VALUE);
        TextField jerseyField = new TextField(player.getJerseyNumber() != null ? player.getJerseyNumber().toString() : "");
        jerseyField.setMaxWidth(Double.MAX_VALUE);
        TextField positionField = new TextField(player.getPosition());
        positionField.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Team> teamCombo = new ComboBox<>(FXCollections.observableArrayList(teamService.findAll()));
        teamCombo.setMaxWidth(Double.MAX_VALUE);
        configureTeamCombo(teamCombo);
        teamCombo.setValue(player.getTeam());

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Jersey #:"), 0, 2);
        grid.add(jerseyField, 1, 2);
        grid.add(new Label("Position:"), 0, 3);
        grid.add(positionField, 1, 3);
        grid.add(new Label("Team:"), 0, 4);
        grid.add(teamCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Make dialog resizable and persist size
        dialog.getDialogPane().getScene().getWindow().setOnShown(e ->
            DialogUtil.makeResizable(dialog, "player.edit", 600, 500));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
                    showError("Validation", "First name and last name are required");
                    return null;
                }
                player.setFirstName(firstNameField.getText());
                player.setLastName(lastNameField.getText());
                try {
                    if (!jerseyField.getText().isBlank()) {
                        player.setJerseyNumber(Integer.parseInt(jerseyField.getText()));
                    } else {
                        player.setJerseyNumber(null);
                    }
                } catch (NumberFormatException ex) {
                    showError("Validation", "Jersey number must be a valid number");
                    return null;
                }
                player.setPosition(positionField.getText());
                player.setTeam(teamCombo.getValue());
                return player;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            playerService.update(player.getId(), updated);
            // Re-navigate to refresh the view with updated data
            Player refreshedPlayer = playerService.findById(player.getId())
                .orElse(player);
            NavigationContext newContext = new NavigationContext()
                .navigateTo("rosters", "Rosters")
                .navigateTo("player-detail", refreshedPlayer.getFullName(), refreshedPlayer);
            navigationHandler.navigate(newContext);
        });
    }

    private void deletePlayer(Player player, NavigationContext currentContext) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Player");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete player: " + player.getFullName() + "?\n\nThis action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                playerService.deleteById(player.getId());
                // Navigate back to roster list
                NavigationContext newContext = new NavigationContext()
                    .navigateTo("rosters", "Rosters");
                navigationHandler.navigate(newContext);
            }
        });
    }

    private void configureTeamCombo(ComboBox<Team> combo) {
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Team object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public Team fromString(String string) { return null; }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Team item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

