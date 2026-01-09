package com.scheduleengine;

import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.league.LeagueView;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.team.TeamView;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.FieldView;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.season.SeasonView;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.game.GameView;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.player.RosterView;
import com.scheduleengine.tournament.service.TournamentService;
import com.scheduleengine.tournament.service.TournamentRegistrationService;
import com.scheduleengine.tournament.TournamentView;
import com.scheduleengine.common.service.ScheduleGeneratorService;
import org.springframework.stereotype.Component;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

@Component
public class MainView {
    
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final GameService gameService;
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final PlayerService playerService;
    private final TournamentService tournamentService;
    private final TournamentRegistrationService tournamentRegistrationService;

    private LeagueView leagueView;
    private TeamView teamView;
    private FieldView fieldView;
    private SeasonView seasonView;
    private GameView gameView;
    private RosterView rosterView;
    private TournamentView tournamentView;

    public MainView(LeagueService leagueService, TeamService teamService,
                   FieldService fieldService, SeasonService seasonService,
                   GameService gameService, ScheduleGeneratorService scheduleGeneratorService,
                   PlayerService playerService, TournamentService tournamentService,
                   TournamentRegistrationService tournamentRegistrationService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.gameService = gameService;
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.playerService = playerService;
        this.tournamentService = tournamentService;
        this.tournamentRegistrationService = tournamentRegistrationService;
    }
    
    private StackPane contentArea;
    private String currentView = "leagues";
    private final java.util.Map<String, Button> navButtons = new java.util.HashMap<>();

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
        tournamentView = new TournamentView(tournamentService, tournamentRegistrationService, leagueService, teamService);

        // Create sidebar navigation
        VBox sidebar = createSidebar();
        sidebar.setStyle("-fx-background-color: #2c3e50; -fx-padding: 0;");

        // Wrap sidebar in ScrollPane for scrolling
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setStyle("-fx-background-color: #2c3e50; -fx-background: #2c3e50;");
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setPrefWidth(220);
        sidebarScroll.setMinWidth(220);
        sidebarScroll.setMaxWidth(220);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide scrollbar but keep scroll behavior

        // CSS to hide scrollbar completely
        sidebarScroll.setStyle(
            "-fx-background-color: #2c3e50;" +
            "-fx-background: #2c3e50;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #ecf0f1;");

        // Show leagues view by default
        showView("leagues");

        root.setLeft(sidebarScroll);
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
            createSectionHeader("DATA MANAGEMENT", FontAwesomeIcon.DATABASE),
            createNavButton(FontAwesomeIcon.TROPHY, "Leagues", "leagues"),
            createNavButton(FontAwesomeIcon.USERS, "Teams", "teams"),
            createNavButton(FontAwesomeIcon.USER, "Rosters", "rosters"),
            createNavButton(FontAwesomeIcon.MAP_MARKER, "Fields", "fields"),
            createNavButton(FontAwesomeIcon.CALENDAR, "Seasons", "seasons"),
            createNavButton(FontAwesomeIcon.CERTIFICATE, "Tournaments", "tournaments"),
            createNavButton(FontAwesomeIcon.FUTBOL_ALT, "Games", "games"),
            createSeparator(),
            createSectionHeader("REGISTRATION", FontAwesomeIcon.EDIT),
            createNavButton(FontAwesomeIcon.CLIPBOARD, "Team Registration", "registration"),
            createSeparator(),
            createSectionHeader("PAYMENTS", FontAwesomeIcon.MONEY),
            createNavButton(FontAwesomeIcon.CREDIT_CARD, "Payment Management", "payments"),
            createSeparator(),
            createSectionHeader("OPERATIONS", FontAwesomeIcon.COG),
            createNavButton(FontAwesomeIcon.BAR_CHART, "Game Operations", "operations")
        );

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer with menu
        VBox footer = new VBox(2);
        footer.setStyle("-fx-padding: 10 0;");
        footer.getChildren().addAll(
            createSeparator(),
            createNavButton(FontAwesomeIcon.SIGN_OUT, "Exit", "exit")
        );

        sidebar.getChildren().addAll(header, navItems, spacer, footer);
        return sidebar;
    }

    private Button createNavButton(FontAwesomeIcon icon, String text, String viewId) {
        Button btn = new Button();

        HBox content = new HBox(12);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16");
        iconView.setFill(javafx.scene.paint.Color.web("#ecf0f1"));

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ecf0f1;");

        content.getChildren().addAll(iconView, textLabel);
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
            updateNavButtonStyles(viewId);
        });

        // Set initial active state
        if (viewId.equals(currentView)) {
            btn.setStyle("-fx-background-color: #667eea; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
        }

        // Store button reference for later highlighting
        navButtons.put(viewId, btn);

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

    private HBox createSectionHeader(String text, FontAwesomeIcon icon) {
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 10 20 5 20;");

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("12");
        iconView.setFill(javafx.scene.paint.Color.web("#95a5a6"));

        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");

        header.getChildren().addAll(iconView, label);
        return header;
    }

    private void updateNavButtonStyles(String viewId) {
        currentView = viewId;

        // Reset all buttons to transparent
        for (Button btn : navButtons.values()) {
            btn.setStyle("-fx-background-color: transparent; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
        }

        // Highlight the active button
        Button activeBtn = navButtons.get(viewId);
        if (activeBtn != null) {
            activeBtn.setStyle("-fx-background-color: #667eea; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
        }
    }

    private void updateButtonStyles(javafx.scene.Parent parent, String activeViewId) {
        // This method is no longer needed but keeping for compatibility
    }

    private void showView(String viewId) {
        contentArea.getChildren().clear();

        switch (viewId) {
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
            case "tournaments":
                tournamentView.refresh();
                contentArea.getChildren().add(tournamentView.getView());
                break;
            case "games":
                gameView.refresh();
                contentArea.getChildren().add(gameView.getView());
                break;
            case "registration":
                contentArea.getChildren().add(createRegistrationView());
                break;
            case "payments":
                contentArea.getChildren().add(createPaymentsView());
                break;
            case "operations":
                contentArea.getChildren().add(createOperationsView());
                break;
            case "exit":
                javafx.application.Platform.exit();
                break;
        }
    }

    private VBox createRegistrationView() {
        VBox vbox = new VBox(30);
        vbox.setPadding(new Insets(40));
        vbox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        VBox.setMargin(vbox, new Insets(20));

        Label titleLabel = new Label("📝 Team Registration");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label("Streamlined team and roster management interface");
        descLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);

        Label comingSoonLabel = new Label("Coming Soon");
        comingSoonLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #95a5a6; -fx-padding: 40 0 0 0;");

        Label detailsLabel = new Label("This view will provide:\n\n" +
                "• Quick team registration and management\n" +
                "• Roster creation and player assignments\n" +
                "• Team status tracking\n" +
                "• Bulk operations for multiple teams");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        detailsLabel.setWrapText(true);

        vbox.getChildren().addAll(titleLabel, descLabel, comingSoonLabel, detailsLabel);
        return vbox;
    }

    private VBox createPaymentsView() {
        VBox vbox = new VBox(30);
        vbox.setPadding(new Insets(40));
        vbox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        VBox.setMargin(vbox, new Insets(20));

        Label titleLabel = new Label("💰 Payment Management");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label("Track and manage all financial transactions");
        descLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);

        Label comingSoonLabel = new Label("Coming Soon");
        comingSoonLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #95a5a6; -fx-padding: 40 0 0 0;");

        Label detailsLabel = new Label("This view will provide:\n\n" +
                "• Team registration fee tracking\n" +
                "• Tournament entry fee management\n" +
                "• Payment status and history\n" +
                "• Financial reporting and analytics");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        detailsLabel.setWrapText(true);

        vbox.getChildren().addAll(titleLabel, descLabel, comingSoonLabel, detailsLabel);
        return vbox;
    }

    private VBox createOperationsView() {
        VBox vbox = new VBox(30);
        vbox.setPadding(new Insets(40));
        vbox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        VBox.setMargin(vbox, new Insets(20));

        Label titleLabel = new Label("⚙️ Game Operations");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descLabel = new Label("Real-time game tracking and statistics management");
        descLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);

        Label comingSoonLabel = new Label("Coming Soon");
        comingSoonLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #95a5a6; -fx-padding: 40 0 0 0;");

        Label detailsLabel = new Label("This view will provide:\n\n" +
                "• Live game score updates\n" +
                "• Quick status changes (Scheduled → In Progress → Completed)\n" +
                "• Multi-game dashboard view\n" +
                "• Player statistics tracking\n" +
                "• Game event logging");
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        detailsLabel.setWrapText(true);

        vbox.getChildren().addAll(titleLabel, descLabel, comingSoonLabel, detailsLabel);
        return vbox;
    }
}
