package com.scheduleengine;

import com.scheduleengine.service.*;
import org.springframework.stereotype.Component;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

@Component
public class MainView {
    
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final GameService gameService;
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final PlayerService playerService;

    private LeagueView leagueView;
    private TeamView teamView;
    private FieldView fieldView;
    private SeasonView seasonView;
    private GameView gameView;
    private RosterView rosterView;

    public MainView(LeagueService leagueService, TeamService teamService,
                   FieldService fieldService, SeasonService seasonService,
                   GameService gameService, ScheduleGeneratorService scheduleGeneratorService,
                   PlayerService playerService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.gameService = gameService;
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.playerService = playerService;
    }
    
    private StackPane contentArea;
    private String currentView = "home";

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Schedule Engine");
        
        // Create main layout
        BorderPane root = new BorderPane();

        // Create views
        leagueView = new LeagueView(leagueService);
        teamView = new TeamView(teamService, leagueService);
        fieldView = new FieldView(fieldService);
        gameView = new GameView(gameService, teamService, fieldService, seasonService, leagueService);
        seasonView = new SeasonView(seasonService, leagueService, scheduleGeneratorService, gameView);
        rosterView = new RosterView(playerService, teamService);

        // Create sidebar navigation
        VBox sidebar = createSidebar();
        sidebar.setStyle("-fx-background-color: #2c3e50; -fx-padding: 0;");
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #ecf0f1;");

        // Show home view by default
        showView("home");

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setSpacing(0);

        // Header
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: #1a252f; -fx-padding: 25 20;");
        Label appTitle = new Label("Schedule Engine");
        appTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label appSubtitle = new Label("Sports Management");
        appSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
        header.getChildren().addAll(appTitle, appSubtitle);

        // Navigation items
        VBox navItems = new VBox(2);
        navItems.setStyle("-fx-padding: 10 0;");

        navItems.getChildren().addAll(
            createNavButton("🏠", "Home", "home"),
            createSeparator(),
            createNavButton("🏆", "Leagues", "leagues"),
            createNavButton("👥", "Teams", "teams"),
            createNavButton("👤", "Rosters", "rosters"),
            createNavButton("📍", "Fields", "fields"),
            createNavButton("📅", "Seasons", "seasons"),
            createNavButton("⚽", "Games", "games")
        );

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer with menu
        VBox footer = new VBox(2);
        footer.setStyle("-fx-padding: 10 0;");
        footer.getChildren().addAll(
            createSeparator(),
            createNavButton("ℹ️", "About", "about"),
            createNavButton("🚪", "Exit", "exit")
        );

        sidebar.getChildren().addAll(header, navItems, spacer, footer);
        return sidebar;
    }

    private Button createNavButton(String icon, String text, String viewId) {
        Button btn = new Button();

        HBox content = new HBox(12);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ecf0f1;");

        content.getChildren().addAll(iconLabel, textLabel);
        btn.setGraphic(content);

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");

        btn.setOnMouseEntered(e -> {
            if (!currentView.equals(viewId)) {
                btn.setStyle("-fx-background-color: #34495e; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!currentView.equals(viewId)) {
                btn.setStyle("-fx-background-color: transparent; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
            }
        });

        btn.setOnAction(e -> {
            showView(viewId);
            updateNavButtonStyles(btn, viewId);
        });

        // Set initial active state
        if (viewId.equals(currentView)) {
            btn.setStyle("-fx-background-color: #667eea; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
        }

        return btn;
    }

    private Region createSeparator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #34495e; -fx-padding: 0 10;");
        VBox container = new VBox(sep);
        container.setStyle("-fx-padding: 8 20;");
        return container;
    }

    private void updateNavButtonStyles(Button activeBtn, String viewId) {
        currentView = viewId;
        VBox sidebar = (VBox) activeBtn.getParent().getParent();
        updateButtonStyles(sidebar, viewId);
    }

    private void updateButtonStyles(javafx.scene.Parent parent, String activeViewId) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                // Reset all buttons
                btn.setStyle("-fx-background-color: transparent; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
            } else if (node instanceof javafx.scene.Parent) {
                updateButtonStyles((javafx.scene.Parent) node, activeViewId);
            }
        }
    }

    private void showView(String viewId) {
        contentArea.getChildren().clear();

        switch (viewId) {
            case "home":
                contentArea.getChildren().add(createHomeView());
                break;
            case "leagues":
                leagueView.refresh();
                contentArea.getChildren().add(leagueView.getView());
                break;
            case "teams":
                teamView.refresh();
                contentArea.getChildren().add(teamView.getView());
                break;
            case "rosters":
                rosterView.refresh();
                contentArea.getChildren().add(rosterView.getView());
                break;
            case "fields":
                fieldView.refresh();
                contentArea.getChildren().add(fieldView.getView());
                break;
            case "seasons":
                seasonView.refresh();
                contentArea.getChildren().add(seasonView.getView());
                break;
            case "games":
                gameView.refresh();
                contentArea.getChildren().add(gameView.getView());
                break;
            case "about":
                showAboutDialog();
                break;
            case "exit":
                javafx.application.Platform.exit();
                break;
        }
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Schedule Engine");
        alert.setContentText("A modern scheduling system for managing leagues, teams, fields, and games.\n\nVersion 0.1\nBuilt with Spring Boot & JavaFX");
        alert.showAndWait();
    }
    
    private VBox createHomeView() {
        VBox vbox = new VBox(30);
        vbox.setPadding(new Insets(40));
        vbox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        VBox.setMargin(vbox, new Insets(20));

        Label titleLabel = new Label("Welcome to Schedule Engine");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label("A modern scheduling system for managing leagues, teams, fields, and games.");
        descLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);
        
        // Stats cards in a grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setPadding(new Insets(20, 0, 0, 0));

        statsGrid.add(createStatCard("🏆", "Leagues", String.valueOf(leagueService.findAll().size()), "#667eea"), 0, 0);
        statsGrid.add(createStatCard("👥", "Teams", String.valueOf(teamService.findAll().size()), "#764ba2"), 1, 0);
        statsGrid.add(createStatCard("📍", "Fields", String.valueOf(fieldService.findAll().size()), "#f093fb"), 0, 1);
        statsGrid.add(createStatCard("📅", "Seasons", String.valueOf(seasonService.findAll().size()), "#4facfe"), 1, 1);
        statsGrid.add(createStatCard("⚽", "Games", String.valueOf(gameService.findAll().size()), "#43e97b"), 0, 2, 2, 1);

        vbox.getChildren().addAll(titleLabel, descLabel, statsGrid);
        return vbox;
    }
    
    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(250);
        card.setPrefHeight(120);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        card.getChildren().addAll(iconLabel, nameLabel, valueLabel);
        return card;
    }
}
