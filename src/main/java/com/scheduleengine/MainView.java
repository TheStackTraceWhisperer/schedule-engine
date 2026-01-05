package com.scheduleengine;

import com.scheduleengine.service.*;
import jakarta.inject.Singleton;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

@Singleton
public class MainView {
    
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final GameService gameService;
    
    private TabPane tabPane;
    
    public MainView(LeagueService leagueService, TeamService teamService,
                   FieldService fieldService, SeasonService seasonService,
                   GameService gameService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.gameService = gameService;
    }
    
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Schedule Engine");
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create menu bar
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);
        
        // Create tab pane
        tabPane = new TabPane();
        
        Tab homeTab = new Tab("Home", createHomeView());
        homeTab.setClosable(false);
        
        Tab leaguesTab = new Tab("Leagues", createLeaguesView());
        leaguesTab.setClosable(false);
        
        Tab teamsTab = new Tab("Teams", createTeamsView());
        teamsTab.setClosable(false);
        
        Tab fieldsTab = new Tab("Fields", createFieldsView());
        fieldsTab.setClosable(false);
        
        Tab seasonsTab = new Tab("Seasons", createSeasonsView());
        seasonsTab.setClosable(false);
        
        Tab gamesTab = new Tab("Games", createGamesView());
        gamesTab.setClosable(false);
        
        tabPane.getTabs().addAll(homeTab, leaguesTab, teamsTab, fieldsTab, seasonsTab, gamesTab);
        
        root.setCenter(tabPane);
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> javafx.application.Platform.exit());
        fileMenu.getItems().add(exitItem);
        
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Schedule Engine");
            alert.setContentText("A scheduling system for managing leagues, teams, fields, and games.\nVersion 0.1");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }
    
    private VBox createHomeView() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Welcome to Schedule Engine");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label descLabel = new Label("A modern scheduling system for managing leagues, teams, fields, and games.");
        descLabel.setWrapText(true);
        
        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(20));
        statsGrid.setStyle("-fx-background-color: #f4f7f9; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        int row = 0;
        addStatRow(statsGrid, row++, "Leagues:", String.valueOf(leagueService.findAll().size()));
        addStatRow(statsGrid, row++, "Teams:", String.valueOf(teamService.findAll().size()));
        addStatRow(statsGrid, row++, "Fields:", String.valueOf(fieldService.findAll().size()));
        addStatRow(statsGrid, row++, "Seasons:", String.valueOf(seasonService.findAll().size()));
        addStatRow(statsGrid, row++, "Games:", String.valueOf(gameService.findAll().size()));
        
        vbox.getChildren().addAll(titleLabel, descLabel, new Label(), statsGrid);
        return vbox;
    }
    
    private void addStatRow(GridPane grid, int row, String label, String value) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #667eea;");
        
        grid.add(nameLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }
    
    private VBox createLeaguesView() {
        return new LeagueView(leagueService).getView();
    }
    
    private VBox createTeamsView() {
        return new TeamView(teamService, leagueService).getView();
    }
    
    private VBox createFieldsView() {
        return new FieldView(fieldService).getView();
    }
    
    private VBox createSeasonsView() {
        return new SeasonView(seasonService, leagueService).getView();
    }
    
    private VBox createGamesView() {
        return new GameView(gameService, teamService, fieldService, seasonService).getView();
    }
}
