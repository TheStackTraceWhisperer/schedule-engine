package com.scheduleengine.game;

import com.scheduleengine.game.domain.Game;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameDetailView {
  private final GameService gameService;
  private final NavigationHandler navigationHandler;

  public GameDetailView(GameService gameService, NavigationHandler navigationHandler) {
    this.gameService = gameService;
    this.navigationHandler = navigationHandler;
  }

  public VBox getView(Game game) {
    VBox root = new VBox(12);
    root.setPadding(new Insets(16));

    Label title = new Label("Game #" + game.getId());
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    GridPane details = new GridPane();
    details.setHgap(10);
    details.setVgap(8);

    int r = 0;
    details.add(new Label("Date/Time:"), 0, r);
    details.add(new Label(game.getGameDate() != null ? game.getGameDate().toString() : ""), 1, r++);
    details.add(new Label("Home Team:"), 0, r);
    details.add(new Label(game.getHomeTeam() != null ? game.getHomeTeam().getName() : ""), 1, r++);
    details.add(new Label("Away Team:"), 0, r);
    details.add(new Label(game.getAwayTeam() != null ? game.getAwayTeam().getName() : ""), 1, r++);
    details.add(new Label("Field:"), 0, r);
    details.add(new Label(game.getField() != null ? game.getField().getName() : ""), 1, r++);
    details.add(new Label("Season:"), 0, r);
    details.add(new Label(game.getSeason() != null ? game.getSeason().getName() : ""), 1, r++);
    details.add(new Label("Status:"), 0, r);
    details.add(new Label(game.getStatus() != null ? game.getStatus().name() : ""), 1, r++);
    details.add(new Label("Score:"), 0, r);
    String score = (game.getHomeScore() != null ? game.getHomeScore() : 0) + " - " + (game.getAwayScore() != null ? game.getAwayScore() : 0);
    details.add(new Label(score), 1, r++);

    // Actions
    HBox actions = new HBox(8);
    Button editBtn = new Button("Edit");
    editBtn.setOnAction(e -> showEditDialog(game));

    Button deleteBtn = new Button("Delete");
    deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
    deleteBtn.setOnAction(e -> deleteGame(game));

    Button backBtn = new Button("Back to Games");
    backBtn.setOnAction(e -> {
      NavigationContext ctx = new NavigationContext().navigateTo("games", "Games");
      navigationHandler.navigate(ctx);
    });

    actions.getChildren().addAll(editBtn, deleteBtn, backBtn);

    root.getChildren().addAll(title, new Separator(), details, new Separator(), actions);
    return root;
  }

  private void showEditDialog(Game game) {
    // reuse GameView's edit form via a small dialog for now
    Dialog<Game> dialog = new Dialog<>();
    dialog.setTitle("Edit Game");
    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
    GameView temp = new GameView(gameService, null, null, null, null);
    GridPane form = temp.buildGameForm(game);
    dialog.getDialogPane().setContent(form);
    dialog.setResultConverter(btn -> btn == saveButtonType ? temp.buildGameFromForm(form, game) : null);
    dialog.showAndWait().ifPresent(updated -> {
      gameService.update(game.getId(), updated);
      // Navigate to updated detail
      NavigationContext ctx = new NavigationContext().navigateTo("games", "Games").navigateTo("game-detail", "Game #" + updated.getId(), updated);
      navigationHandler.navigate(ctx);
    });
  }

  private void deleteGame(Game game) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete Game");
    alert.setHeaderText("Are you sure?");
    alert.setContentText("Do you want to delete game #" + game.getId() + "?");
    alert.showAndWait().ifPresent(resp -> {
      if (resp == ButtonType.OK) {
        gameService.deleteById(game.getId());
        NavigationContext ctx = new NavigationContext().navigateTo("games", "Games");
        navigationHandler.navigate(ctx);
      }
    });
  }
}

