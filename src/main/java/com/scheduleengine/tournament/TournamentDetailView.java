package com.scheduleengine.tournament;

import com.scheduleengine.navigation.NavigationContext;
import com.scheduleengine.navigation.NavigationHandler;
import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.service.TournamentService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TournamentDetailView {
  private final TournamentService tournamentService;
  private final NavigationHandler navigationHandler;

  public TournamentDetailView(TournamentService tournamentService, NavigationHandler navigationHandler) {
    this.tournamentService = tournamentService;
    this.navigationHandler = navigationHandler;
  }

  public VBox getView(Tournament t) {
    VBox root = new VBox(12);
    root.setPadding(new Insets(16));

    Label title = new Label("Tournament: " + t.getName());
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    GridPane details = new GridPane();
    details.setHgap(10);
    details.setVgap(8);

    int r = 0;
    details.add(new Label("Type:"), 0, r);
    details.add(new Label(t.getType() != null ? t.getType().name() : ""), 1, r++);
    details.add(new Label("League:"), 0, r);
    details.add(new Label(t.getLeague() != null ? t.getLeague().getName() : "Open/All"), 1, r++);
    details.add(new Label("Start Date:"), 0, r);
    details.add(new Label(t.getStartDate() != null ? t.getStartDate().toString() : ""), 1, r++);
    details.add(new Label("End Date:"), 0, r);
    details.add(new Label(t.getEndDate() != null ? t.getEndDate().toString() : ""), 1, r++);
    details.add(new Label("Status:"), 0, r);
    details.add(new Label(t.getStatus() != null ? t.getStatus().name() : ""), 1, r++);
    details.add(new Label("Max Teams:"), 0, r);
    details.add(new Label(t.getMaxTeams() != null ? t.getMaxTeams().toString() : "-"), 1, r++);
    details.add(new Label("Entry Fee:"), 0, r);
    details.add(new Label(t.getEntryFee() != null ? String.format("$%.2f", t.getEntryFee()) : "-"), 1, r++);
    details.add(new Label("Location:"), 0, r);
    details.add(new Label(t.getLocation() != null ? t.getLocation() : ""), 1, r++);
    details.add(new Label("Description:"), 0, r);
    Label desc = new Label(t.getDescription() != null ? t.getDescription() : "");
    desc.setWrapText(true);
    details.add(desc, 1, r++);

    HBox actions = new HBox(8);
    Button editBtn = new Button("Edit");
    editBtn.setOnAction(e -> showEditDialog(t));

    Button deleteBtn = new Button("Delete");
    deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
    deleteBtn.setOnAction(e -> deleteTournament(t));

    Button backBtn = new Button("Back to Tournaments");
    backBtn.setOnAction(e -> navigationHandler.navigate(new NavigationContext().navigateTo("tournaments", "Tournaments")));

    actions.getChildren().addAll(editBtn, deleteBtn, backBtn);

    root.getChildren().addAll(title, new Separator(), details, new Separator(), actions);
    return root;
  }

  private void showEditDialog(Tournament existing) {
    Dialog<Tournament> dialog = new Dialog<>();
    dialog.setTitle("Edit Tournament");
    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField nameField = new TextField(existing.getName());
    TextArea descField = new TextArea(existing.getDescription() != null ? existing.getDescription() : "");
    descField.setPrefRowCount(3);
    ComboBox<Tournament.TournamentType> typeCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(Tournament.TournamentType.values()));
    typeCombo.setValue(existing.getType());
    DatePicker startPicker = new DatePicker(existing.getStartDate());
    DatePicker endPicker = new DatePicker(existing.getEndDate());
    TextField entryFeeField = new TextField(existing.getEntryFee() != null ? existing.getEntryFee().toString() : "0.00");
    TextField locationField = new TextField(existing.getLocation() != null ? existing.getLocation() : "");

    int row = 0;
    grid.add(new Label("Name:"), 0, row);
    grid.add(nameField, 1, row++);
    grid.add(new Label("Description:"), 0, row);
    grid.add(descField, 1, row++);
    grid.add(new Label("Type:"), 0, row);
    grid.add(typeCombo, 1, row++);
    grid.add(new Label("Start Date:"), 0, row);
    grid.add(startPicker, 1, row++);
    grid.add(new Label("End Date:"), 0, row);
    grid.add(endPicker, 1, row++);
    grid.add(new Label("Entry Fee:"), 0, row);
    grid.add(entryFeeField, 1, row++);
    grid.add(new Label("Location:"), 0, row);
    grid.add(locationField, 1, row++);

    dialog.getDialogPane().setContent(grid);
    dialog.setResultConverter(btn -> {
      if (btn == saveButtonType) {
        existing.setName(nameField.getText());
        existing.setDescription(descField.getText());
        existing.setType(typeCombo.getValue());
        existing.setStartDate(startPicker.getValue());
        existing.setEndDate(endPicker.getValue());
        try {
          existing.setEntryFee(Double.parseDouble(entryFeeField.getText()));
        } catch (NumberFormatException e) {
          existing.setEntryFee(0.0);
        }
        existing.setLocation(locationField.getText());
        return existing;
      }
      return null;
    });

    dialog.showAndWait().ifPresent(updated -> {
      tournamentService.update(existing.getId(), updated);
      navigationHandler.navigate(new NavigationContext().navigateTo("tournaments", "Tournaments").navigateTo("tournament-detail", updated.getName(), updated));
    });
  }

  private void deleteTournament(Tournament t) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete Tournament");
    alert.setHeaderText("Are you sure?");
    alert.setContentText("Delete tournament '" + t.getName() + "'?");
    alert.showAndWait().ifPresent(resp -> {
      if (resp == ButtonType.OK) {
        tournamentService.deleteById(t.getId());
        navigationHandler.navigate(new NavigationContext().navigateTo("tournaments", "Tournaments"));
      }
    });
  }
}

