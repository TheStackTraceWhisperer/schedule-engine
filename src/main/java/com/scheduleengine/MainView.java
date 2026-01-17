package com.scheduleengine;

import com.scheduleengine.game.GameDetailView;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.league.LeagueView;
import com.scheduleengine.league.LeagueDetailView;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.team.TeamView;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.FieldView;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldUsageBlockService;
import com.scheduleengine.season.domain.Season;
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
import com.scheduleengine.common.WindowPreferencesUtil;
import com.scheduleengine.common.UIScaleUtil;
import com.scheduleengine.common.UIScaleControlPanel;
import com.scheduleengine.navigation.*;
import org.springframework.stereotype.Component;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import com.scheduleengine.payment.PaymentsView;
import com.scheduleengine.payment.service.TransactionService;

@Component
public class MainView {

    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final FieldAvailabilityService fieldAvailabilityService;
    private final FieldUsageBlockService fieldUsageBlockService;
    private final SeasonService seasonService;
    private final GameService gameService;
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final PlayerService playerService;
    private final TournamentService tournamentService;
    private final TournamentRegistrationService tournamentRegistrationService;
    private final TransactionService transactionService;

    private LeagueView leagueView;
    private LeagueDetailView leagueDetailView;
    private TeamView teamView;
    private com.scheduleengine.team.TeamDetailView teamDetailView;
    private FieldView fieldView;
    private com.scheduleengine.field.FieldDetailView fieldDetailView;
    private SeasonView seasonView;
    private com.scheduleengine.season.SeasonDetailView seasonDetailView;
    private GameView gameView;
    private RosterView rosterView;
    private com.scheduleengine.player.PlayerDetailView playerDetailView;
    private TournamentView tournamentView;
    private PaymentsView paymentsView;

    private NavigationContext currentNavigationContext;
    private BreadcrumbBar breadcrumbBar;

    public MainView(LeagueService leagueService, TeamService teamService,
                   FieldService fieldService, SeasonService seasonService,
                   GameService gameService, ScheduleGeneratorService scheduleGeneratorService,
                   PlayerService playerService, TournamentService tournamentService,
                   TournamentRegistrationService tournamentRegistrationService,
                   FieldAvailabilityService fieldAvailabilityService,
                   FieldUsageBlockService fieldUsageBlockService,
                   TransactionService transactionService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.gameService = gameService;
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.playerService = playerService;
        this.tournamentService = tournamentService;
        this.tournamentRegistrationService = tournamentRegistrationService;
        this.fieldAvailabilityService = fieldAvailabilityService;
        this.fieldUsageBlockService = fieldUsageBlockService;
        this.transactionService = transactionService;
    }

    private StackPane contentArea;
    private String currentView = "leagues";
    private final java.util.Map<String, Button> navButtons = new java.util.HashMap<>();

    // Navigation history: tracks the last navigation context per top-level view
    // e.g., "leagues" -> last breadcrumb when viewing leagues
    //       "teams" -> last breadcrumb when viewing teams
    private final java.util.Map<String, NavigationContext> navigationHistory = new java.util.HashMap<>();

    // UI Scale Control Panel reference for updating scene after creation
    private UIScaleControlPanel uiScaleControl;

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Schedule Engine");

        // Restore window state from preferences before showing
        WindowPreferencesUtil.restoreWindowState(primaryStage);

        // Initialize navigation context
        currentNavigationContext = new NavigationContext();

        // Create main layout
        BorderPane root = new BorderPane();

        // Create views
        leagueView = new LeagueView(leagueService);
        leagueView.setNavigationHandler(this::navigate);
        leagueDetailView = new LeagueDetailView(leagueService, this::navigate);
        teamView = new TeamView(teamService, leagueService);
        teamView.setNavigationHandler(this::navigate);
        teamDetailView = new com.scheduleengine.team.TeamDetailView(teamService, leagueService, this::navigate);
        fieldView = new FieldView(fieldService, fieldAvailabilityService, fieldUsageBlockService, gameService);
        fieldView.setNavigationHandler(this::navigate);
        fieldDetailView = new com.scheduleengine.field.FieldDetailView(fieldService, fieldAvailabilityService, fieldUsageBlockService, gameService);
        fieldDetailView.setNavigationHandler(this::navigate);
        gameView = new GameView(gameService, teamService, fieldService, seasonService, leagueService);
        gameView.setNavigationHandler(this::navigate);
        seasonView = new SeasonView(seasonService, leagueService, scheduleGeneratorService, gameView, gameService);
        seasonView.setNavigationHandler(this::navigate);
        seasonDetailView = new com.scheduleengine.season.SeasonDetailView(seasonService, leagueService, this::navigate, scheduleGeneratorService, gameService);
        rosterView = new RosterView(playerService, teamService, this::navigate);
        playerDetailView = new com.scheduleengine.player.PlayerDetailView(playerService, teamService, this::navigate);
        tournamentView = new TournamentView(tournamentService, tournamentRegistrationService, leagueService, teamService);
        tournamentView.setNavigationHandler(this::navigate);
        paymentsView = new PaymentsView(transactionService, teamService, playerService, leagueService, tournamentService);
        paymentsView.setNavigationHandler(this::navigate);

        // Create breadcrumb bar
        breadcrumbBar = new BreadcrumbBar(this::navigate);

        // Create content container with breadcrumb
        VBox contentContainer = new VBox();
        contentContainer.getChildren().add(breadcrumbBar);

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #ecf0f1;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        contentContainer.getChildren().add(contentArea);

        // Show leagues view by default
        navigate(currentNavigationContext.navigateTo("leagues", "Leagues"));

        root.setCenter(contentContainer);

        Scene scene = new Scene(root, 1200, 750);

        // Apply global stylesheet that respects UI scale
        applyGlobalScaling(scene);

        // Create sidebar AFTER scene is ready so we can pass it to UIScaleControlPanel
        VBox sidebar = createSidebar(scene);
        sidebar.setStyle("-fx-background-color: #2c3e50; -fx-padding: 0;");

        // Wrap sidebar in ScrollPane for scrolling
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setStyle("-fx-background-color: #2c3e50; -fx-background: #2c3e50;");
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setPrefWidth(220);
        sidebarScroll.setMinWidth(220);
        sidebarScroll.setMaxWidth(220);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // CSS to hide horizontal scrollbar, allow vertical scrolling
        sidebarScroll.setStyle(
            "-fx-background-color: #2c3e50;" +
            "-fx-background: #2c3e50;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );

        root.setLeft(sidebarScroll);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Setup persistence of window state on close and periodically
        WindowPreferencesUtil.setupWindowStatePersistence(primaryStage);
    }

    /**
     * Apply global CSS styling that respects UI scale settings
     */
    private void applyGlobalScaling(Scene scene) {
        double scale = UIScaleUtil.getScale();

        // Create a global stylesheet that scales fonts and spacing
        String globalCSS = String.format(
            "* { -fx-font-size: %.0fpx; } " +
            ".root { -fx-font-size: %.0fpx; } " +
            ".label { -fx-font-size: %.0fpx; } " +
            ".button { -fx-font-size: %.0fpx; -fx-padding: %.1fem; } " +
            ".text-field { -fx-font-size: %.0fpx; -fx-padding: %.1fem; } " +
            ".combo-box { -fx-font-size: %.0fpx; } " +
            ".table-view { -fx-font-size: %.0fpx; } " +
            ".table-view .table-cell { -fx-padding: %.1fem; }",
            12.0 * scale,
            12.0 * scale,
            13.0 * scale,
            13.0 * scale,
            0.5 * scale,
            12.0 * scale,
            0.5 * scale,
            12.0 * scale,
            12.0 * scale,
            0.2 * scale
        );

        scene.getStylesheets().add("data:text/css," + globalCSS);
    }

    private VBox createSidebar(Scene scene) {
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
            createNavButton(FontAwesomeIcon.TROPHY, "Leagues", "leagues"),
            createNavButton(FontAwesomeIcon.USERS, "Teams", "teams"),
            createNavButton(FontAwesomeIcon.USER, "Rosters", "rosters"),
            createNavButton(FontAwesomeIcon.MAP_MARKER, "Fields", "fields"),
            createNavButton(FontAwesomeIcon.CALENDAR, "Seasons", "seasons"),
            createNavButton(FontAwesomeIcon.CERTIFICATE, "Tournaments", "tournaments"),
            createNavButton(FontAwesomeIcon.FUTBOL_ALT, "Games", "games"),
            createNavButton(FontAwesomeIcon.CLIPBOARD, "Team Registration", "registration"),
            createNavButton(FontAwesomeIcon.CREDIT_CARD, "Payment Management", "payments"),
            createNavButton(FontAwesomeIcon.BAR_CHART, "Game Operations", "operations")
        );

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // UI Scale Control Panel
        uiScaleControl = new UIScaleControlPanel(scene);
        uiScaleControl.setOnScaleChange(() -> {
            // Scale is now applied in real-time by UIScaleControlPanel
        });

        // Footer with menu
        VBox footer = new VBox(2);
        footer.setStyle("-fx-padding: 10 0;");
        footer.getChildren().addAll(
            createSeparator(),
            uiScaleControl,
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
            // Check if we have previous navigation history for this view
            NavigationContext contextToUse = navigationHistory.get(viewId);

            if (contextToUse == null) {
                // No history, create fresh navigation to root level view
                contextToUse = new NavigationContext().navigateTo(viewId, text);
            }

            navigate(contextToUse);
            // updateSidebarButtonStyles() is now called from navigate() method
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


    private void updateButtonStyles(javafx.scene.Parent parent, String activeViewId) {
        // This method is no longer needed but keeping for compatibility
    }

    /**
     * Navigate to a view with the given navigation context
     */
    private void navigate(NavigationContext context) {
        this.currentNavigationContext = context;
        breadcrumbBar.updateContext(context);

        // Clear current content before showing new view
        contentArea.getChildren().clear();

        String viewId = context.getCurrentViewId();
        if (viewId == null) {
            showDashboard();
            return;
        }

        switch (viewId) {
            // League-related views
            case "leagues":
                leagueView.refresh();
                contentArea.getChildren().add(leagueView.getView());
                break;
            case "league-detail":
                League league = context.getContextData("league-detail", League.class);
                if (league != null) {
                    contentArea.getChildren().add(leagueDetailView.getView(league, context));
                }
                break;
            case "league-teams":
                League leagueForTeams = context.getContextData("league-teams", League.class);
                if (leagueForTeams != null) {
                    // Show teams filtered by this league
                    teamView.setFilterLeague(leagueForTeams);
                    teamView.refresh();
                    contentArea.getChildren().add(teamView.getView());
                }
                break;
            case "league-seasons":
                League leagueForSeasons = context.getContextData("league-seasons", League.class);
                if (leagueForSeasons != null) {
                    // Show seasons filtered by this league
                    seasonView.setFilterLeague(leagueForSeasons);
                    seasonView.refresh();
                    contentArea.getChildren().add(seasonView.getView());
                }
                break;
            case "league-stats":
                League leagueForStats = context.getContextData("league-stats", League.class);
                if (leagueForStats != null) {
                    contentArea.getChildren().add(createLeagueStatsView(leagueForStats));
                }
                break;

            // Team-related views
            case "teams":
                teamView.clearFilter();
                teamView.refresh();
                contentArea.getChildren().add(teamView.getView());
                break;
            case "team-detail":
                Team team = context.getContextData("team-detail", Team.class);
                if (team != null) {
                    contentArea.getChildren().add(teamDetailView.getView(team, context));
                }
                break;
            case "team-roster":
                Team teamForRoster = context.getContextData("team-roster", Team.class);
                if (teamForRoster != null) {
                    // Show roster filtered by this team
                    rosterView.setFilterTeam(teamForRoster);
                    rosterView.refresh();
                    contentArea.getChildren().add(rosterView.getView());
                }
                break;
            case "team-games":
                Team teamForGames = context.getContextData("team-games", Team.class);
                if (teamForGames != null) {
                    // Show games filtered by this team
                    gameView.setFilterTeam(teamForGames);
                    gameView.refresh();
                    contentArea.getChildren().add(gameView.getView());
                }
                break;
            case "team-stats":
                Team teamForStats = context.getContextData("team-stats", Team.class);
                if (teamForStats != null) {
                    contentArea.getChildren().add(createTeamStatsView(teamForStats));
                }
                break;

            // Roster-related views
            case "rosters":
                rosterView.clearFilter();
                rosterView.refresh();
                contentArea.getChildren().add(rosterView.getView());
                break;
            case "fields":
                contentArea.getChildren().add(fieldView.getView());
                fieldView.refresh();
                break;
            case "field-detail":
                com.scheduleengine.field.domain.Field field = context.getContextData("field-detail", com.scheduleengine.field.domain.Field.class);
                if (field != null) {
                    contentArea.getChildren().add(fieldDetailView.getView(field, context));
                }
                break;
            case "player-detail":
                com.scheduleengine.player.domain.Player player = context.getContextData("player-detail", com.scheduleengine.player.domain.Player.class);
                if (player != null) {
                    contentArea.getChildren().add(playerDetailView.getView(player, context));
                }
                break;

            // Season-related views
            case "seasons":
                seasonView.clearFilter();
                seasonView.refresh();
                contentArea.getChildren().add(seasonView.getView());
                break;
            case "season-detail":
                Season season = context.getContextData("season-detail", Season.class);
                if (season != null) {
                    contentArea.getChildren().add(seasonDetailView.getView(season, context));
                }
                break;
            case "season-games":
                Season seasonForGames = context.getContextData("season-games", Season.class);
                if (seasonForGames != null) {
                    // Show games filtered by this season and its league
                    gameView.setFilterSeason(seasonForGames);
                    if (seasonForGames.getLeague() != null) {
                        gameView.setFilterLeague(seasonForGames.getLeague());
                    }
                    gameView.refresh();
                    contentArea.getChildren().add(gameView.getView());
                }
                break;
            case "season-teams":
                Season seasonForTeams = context.getContextData("season-teams", Season.class);
                if (seasonForTeams != null && seasonForTeams.getLeague() != null) {
                    // Show teams filtered by this season's league
                    teamView.setFilterLeague(seasonForTeams.getLeague());
                    teamView.refresh();
                    contentArea.getChildren().add(teamView.getView());
                }
                break;
            case "season-standings":
                Season seasonForStandings = context.getContextData("season-standings", Season.class);
                if (seasonForStandings != null) {
                    contentArea.getChildren().add(createSeasonStandingsView(seasonForStandings));
                }
                break;

            // Game-related views
            case "games":
                gameView.clearFilter();
                gameView.refresh();
                contentArea.getChildren().add(gameView.getView());
                break;
            case "game-detail": {
                com.scheduleengine.game.domain.Game game = context.getContextData("game-detail", com.scheduleengine.game.domain.Game.class);
                if (game != null) {
                    GameDetailView gdv = new com.scheduleengine.game.GameDetailView(gameService, this::navigate);
                    contentArea.getChildren().add(gdv.getView(game));
                }
                break;
            }

            // Tournament-related views
            case "tournaments":
                tournamentView.refresh();
                contentArea.getChildren().add(tournamentView.getView());
                break;
            case "tournament-detail": {
                com.scheduleengine.tournament.domain.Tournament t = context.getContextData("tournament-detail", com.scheduleengine.tournament.domain.Tournament.class);
                if (t != null) {
                    com.scheduleengine.tournament.TournamentDetailView tdv = new com.scheduleengine.tournament.TournamentDetailView(tournamentService, this::navigate);
                    contentArea.getChildren().add(tdv.getView(t));
                }
                break;
            }

            // Registration, Payments, Operations
            case "registration":
                contentArea.getChildren().add(createRegistrationView());
                break;
            case "payments":
                contentArea.getChildren().add(paymentsView.getView());
                paymentsView.refresh();
                // Team-filtered navigation
                Team teamForPayments = context.getContextData("payments", Team.class);
                if (teamForPayments != null) {
                    paymentsView.filterByTeam(teamForPayments.getName());
                } else {
                    // League-filtered navigation for due payments
                    League leagueForPayments = context.getContextData("payments", League.class);
                    if (leagueForPayments != null) {
                        paymentsView.filterByLeagueDue(leagueForPayments.getName());
                    } else {
                        paymentsView.clearFilters();
                    }
                }
                break;
            case "operations":
                contentArea.getChildren().add(createOperationsView());
                break;

            // Exit
            case "exit":
                javafx.application.Platform.exit();
                break;

            // Default - show dashboard
            default:
                showDashboard();
                break;
        }

        // Update sidebar button styles to reflect current view
        updateSidebarButtonStyles(viewId);

        // Save this navigation context to history for the top-level view
        // This allows restoring previous state when clicking sidebar buttons
        String topLevelViewId = getTopLevelViewId(viewId);
        navigationHistory.put(topLevelViewId, context);
    }

    /**
     * Update sidebar button styles to highlight the active top-level view
     */
    private void updateSidebarButtonStyles(String viewId) {
        // Clear all button styles first
        navButtons.forEach((id, btn) -> {
            btn.setStyle("-fx-background-color: transparent; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
        });

        // Determine the top-level view ID from the current viewId
        // For drill-down views like "league-detail", "team-games", etc.,
        // highlight the parent sidebar button (leagues, teams, games, etc.)
        String topLevelViewId = getTopLevelViewId(viewId);
        currentView = topLevelViewId;

        // Highlight the active button
        Button activeBtn = navButtons.get(topLevelViewId);
        if (activeBtn != null) {
            activeBtn.setStyle("-fx-background-color: #667eea; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 0;");
        }
    }

    /**
     * Get the top-level view ID from a potentially nested view ID
     * For example: "league-detail" -> "leagues", "team-games" -> "teams", etc.
     */
    private String getTopLevelViewId(String viewId) {
        if (viewId == null) {
            return "leagues";
        }

        switch (viewId) {
            // League-related views -> highlight Leagues
            case "leagues":
            case "league-detail":
            case "league-teams":
            case "league-seasons":
            case "league-stats":
                return "leagues";

            // Team-related views -> highlight Teams
            case "teams":
            case "team-detail":
            case "team-games":
            case "team-roster":
            case "team-stats":
                return "teams";

            // Season-related views -> highlight Seasons
            case "seasons":
            case "season-detail":
            case "season-games":
            case "season-teams":
            case "season-standings":
                return "seasons";

            // Game-related views -> highlight Games
            case "games":
            case "game-detail":
                return "games";

            // Roster-related views -> highlight Rosters
            case "rosters":
            case "player-detail":
                return "rosters";

            // Fields -> highlight Fields
            case "fields":
            case "field-detail":
                return "fields";

            // Tournaments -> highlight Tournaments
            case "tournaments":
            case "tournament-detail":
                return "tournaments";

            // Default
            default:
                return viewId;
        }
    }

    private void showView(String viewId, NavigationContext context) {
        contentArea.getChildren().clear();

        switch (viewId) {
            case "leagues":
                leagueView.refresh();
                contentArea.getChildren().add(leagueView.getView());
                break;
            case "league-detail":
                League league = context.getContextData("league-detail", League.class);
                if (league != null) {
                    contentArea.getChildren().add(leagueDetailView.getView(league, context));
                }
                break;
            case "league-teams":
                League leagueForTeams = context.getContextData("league-teams", League.class);
                if (leagueForTeams != null) {
                    // Show teams filtered by this league
                    teamView.setFilterLeague(leagueForTeams);
                    teamView.refresh();
                    contentArea.getChildren().add(teamView.getView());
                }
                break;
            case "league-seasons":
                League leagueForSeasons = context.getContextData("league-seasons", League.class);
                if (leagueForSeasons != null) {
                    // Show seasons filtered by this league
                    seasonView.setFilterLeague(leagueForSeasons);
                    seasonView.refresh();
                    contentArea.getChildren().add(seasonView.getView());
                }
                break;
            case "league-stats":
                League leagueForStats = context.getContextData("league-stats", League.class);
                if (leagueForStats != null) {
                    contentArea.getChildren().add(createLeagueStatsView(leagueForStats));
                }
                break;
            case "teams":
                teamView.clearFilter();
                teamView.refresh();
                contentArea.getChildren().add(teamView.getView());
                break;
            case "team-detail":
                Team team = context.getContextData("team-detail", Team.class);
                if (team != null) {
                    contentArea.getChildren().add(teamDetailView.getView(team, context));
                }
                break;
            case "team-roster":
                Team teamForRoster = context.getContextData("team-roster", Team.class);
                if (teamForRoster != null) {
                    // Show roster filtered by this team
                    rosterView.setFilterTeam(teamForRoster);
                    rosterView.refresh();
                    contentArea.getChildren().add(rosterView.getView());
                }
                break;
            case "team-games":
                Team teamForGames = context.getContextData("team-games", Team.class);
                if (teamForGames != null) {
                    // Show games filtered by this team
                    gameView.setFilterTeam(teamForGames);
                    gameView.refresh();
                    contentArea.getChildren().add(gameView.getView());
                }
                break;
            case "team-stats":
                Team teamForStats = context.getContextData("team-stats", Team.class);
                if (teamForStats != null) {
                    contentArea.getChildren().add(createTeamStatsView(teamForStats));
                }
                break;
            case "rosters":
                rosterView.clearFilter();
                rosterView.refresh();
                contentArea.getChildren().add(rosterView.getView());
                break;
            case "fields":
                fieldView.refresh();
                contentArea.getChildren().add(fieldView.getView());
                break;
            case "seasons":
                seasonView.clearFilter();
                seasonView.refresh();
                contentArea.getChildren().add(seasonView.getView());
                break;
            case "season-detail":
                Season season = context.getContextData("season-detail", Season.class);
                if (season != null) {
                    contentArea.getChildren().add(seasonDetailView.getView(season, context));
                }
                break;
            case "season-games":
                Season seasonForGames = context.getContextData("season-games", Season.class);
                if (seasonForGames != null) {
                    // Show games filtered by this season and its league
                    gameView.setFilterSeason(seasonForGames);
                    if (seasonForGames.getLeague() != null) {
                        gameView.setFilterLeague(seasonForGames.getLeague());
                    }
                    gameView.refresh();
                    contentArea.getChildren().add(gameView.getView());
                }
                break;
            case "season-teams":
                Season seasonForTeams = context.getContextData("season-teams", Season.class);
                if (seasonForTeams != null && seasonForTeams.getLeague() != null) {
                    // Show teams filtered by this season's league
                    teamView.setFilterLeague(seasonForTeams.getLeague());
                    teamView.refresh();
                    contentArea.getChildren().add(teamView.getView());
                }
                break;
            case "season-standings":
                Season seasonForStandings = context.getContextData("season-standings", Season.class);
                if (seasonForStandings != null) {
                    contentArea.getChildren().add(createSeasonStandingsView(seasonForStandings));
                }
                break;
            case "tournaments":
                tournamentView.refresh();
                contentArea.getChildren().add(tournamentView.getView());
                break;
            case "games":
                gameView.clearFilter();
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
            default:
                showDashboard();
                break;
        }
    }

    private void showDashboard() {
        contentArea.getChildren().clear();

        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(40));
        dashboard.setStyle("-fx-background-color: #ecf0f1;");

        Label welcomeLabel = new Label("🏆 Welcome to Schedule Engine");
        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Select a module from the sidebar to get started");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // Quick access cards
        GridPane quickAccess = new GridPane();
        quickAccess.setHgap(20);
        quickAccess.setVgap(20);
        quickAccess.setPadding(new Insets(20, 0, 0, 0));

        DrillDownCard leaguesCard = new DrillDownCard(
            "Leagues",
            "Manage leagues and their teams",
            FontAwesomeIcon.TROPHY,
            () -> navigate(new NavigationContext().navigateTo("leagues", "Leagues"))
        );

        DrillDownCard seasonsCard = new DrillDownCard(
            "Seasons",
            "Create and manage game seasons",
            FontAwesomeIcon.CALENDAR,
            () -> navigate(new NavigationContext().navigateTo("seasons", "Seasons"))
        );

        DrillDownCard gamesCard = new DrillDownCard(
            "Games",
            "Schedule and track games",
            FontAwesomeIcon.FUTBOL_ALT,
            () -> navigate(new NavigationContext().navigateTo("games", "Games"))
        );

        DrillDownCard tournamentsCard = new DrillDownCard(
            "Tournaments",
            "Organize tournament brackets",
            FontAwesomeIcon.CERTIFICATE,
            () -> navigate(new NavigationContext().navigateTo("tournaments", "Tournaments"))
        );

        quickAccess.add(leaguesCard, 0, 0);
        quickAccess.add(seasonsCard, 1, 0);
        quickAccess.add(gamesCard, 0, 1);
        quickAccess.add(tournamentsCard, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        quickAccess.getColumnConstraints().addAll(col1, col2);

        dashboard.getChildren().addAll(welcomeLabel, subtitleLabel, quickAccess);

        contentArea.getChildren().add(dashboard);
    }

    private VBox createTeamDetailPlaceholder(Team team) {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(30));
        vbox.setStyle("-fx-background-color: white;");

        Label title = new Label("Team: " + team.getName());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label info = new Label("Team detail view coming soon with drill-down to roster, games, and statistics.");
        info.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        vbox.getChildren().addAll(title, info);
        return vbox;
    }

    private VBox createSeasonDetailPlaceholder(Season season) {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(30));
        vbox.setStyle("-fx-background-color: white;");

        Label title = new Label("Season: " + season.getName());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label info = new Label("Season detail view coming soon with drill-down to teams, games, and schedule generation.");
        info.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        vbox.getChildren().addAll(title, info);
        return vbox;
    }

    private VBox createSeasonStandingsView(Season season) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ecf0f1;");

        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label title = new Label("Standings: " + season.getName());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        if (season.getLeague() != null) {
            Label leagueLabel = new Label("League: " + season.getLeague().getName());
            leagueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
            header.getChildren().addAll(title, leagueLabel);
        } else {
            header.getChildren().add(title);
        }

        // Placeholder content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label infoLabel = new Label("Standings will be automatically calculated based on game results.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        infoLabel.setWrapText(true);

        Label comingSoonLabel = new Label("Coming Soon");
        comingSoonLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");

        content.getChildren().addAll(comingSoonLabel, infoLabel);

        container.getChildren().addAll(header, content);

        return container;
    }

    private VBox createLeagueStatsView(League league) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ecf0f1;");

        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label title = new Label("Statistics: " + league.getName());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().add(title);

        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setPadding(new Insets(20));
        statsGrid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        // Count teams and seasons
        long teamCount = league.getTeams() != null ? league.getTeams().size() : 0;
        long seasonCount = league.getSeasons() != null ? league.getSeasons().size() : 0;

        // Team count card
        VBox teamCard = createStatCard("Teams", String.valueOf(teamCount), "#667eea");
        // Season count card
        VBox seasonCard = createStatCard("Seasons", String.valueOf(seasonCount), "#43e97b");
        // Active seasons (example)
        VBox activeCard = createStatCard("Active Seasons", "0", "#fa709a");
        // Total games (example)
        VBox gamesCard = createStatCard("Total Games", "0", "#feca57");

        statsGrid.add(teamCard, 0, 0);
        statsGrid.add(seasonCard, 1, 0);
        statsGrid.add(activeCard, 0, 1);
        statsGrid.add(gamesCard, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        statsGrid.getColumnConstraints().addAll(col1, col2);

        // Info label
        Label infoLabel = new Label("More detailed analytics and reports coming soon!");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 10;");

        container.getChildren().addAll(header, statsGrid, infoLabel);

        return container;
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        card.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(valueLabel, labelLabel);

        return card;
    }

    private VBox createTeamStatsView(Team team) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ecf0f1;");

        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label title = new Label("Statistics: " + team.getName());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        if (team.getLeague() != null) {
            Label leagueLabel = new Label("League: " + team.getLeague().getName());
            leagueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
            header.getChildren().addAll(title, leagueLabel);
        } else {
            header.getChildren().add(title);
        }

        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setPadding(new Insets(20));
        statsGrid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        // Count roster size (placeholder)
        long playerCount = 0; // TODO: Get actual player count from team roster

        // Player count card
        VBox playerCard = createStatCard("Players", String.valueOf(playerCount), "#667eea");
        // Games played card
        VBox gamesCard = createStatCard("Games Played", "0", "#43e97b");
        // Wins card
        VBox winsCard = createStatCard("Wins", "0", "#fa709a");
        // Season card
        VBox seasonCard = createStatCard("Active Seasons", "0", "#feca57");

        statsGrid.add(playerCard, 0, 0);
        statsGrid.add(gamesCard, 1, 0);
        statsGrid.add(winsCard, 0, 1);
        statsGrid.add(seasonCard, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        statsGrid.getColumnConstraints().addAll(col1, col2);

        // Info label
        Label infoLabel = new Label("More detailed analytics and reports coming soon!");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 10;");

        container.getChildren().addAll(header, statsGrid, infoLabel);

        return container;
    }

    private void showView(String viewId) {
        // Legacy method - redirect to new navigation
        navigate(new NavigationContext().navigateTo(viewId, viewId));
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

