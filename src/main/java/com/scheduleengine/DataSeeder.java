package com.scheduleengine;

import com.github.javafaker.Faker;
import com.scheduleengine.field.domain.Field;
import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.field.service.FieldUsageBlockService;
import com.scheduleengine.game.domain.Game;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.payment.domain.Transaction;
import com.scheduleengine.payment.service.TransactionService;
import com.scheduleengine.player.domain.Player;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.tournament.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final Faker faker = new Faker();
  private final LeagueService leagueService;
  private final TeamService teamService;
  private final FieldService fieldService;
  private final SeasonService seasonService;
  private final PlayerService playerService;
  private final TournamentService tournamentService;
  private final GameService gameService;
  private final FieldAvailabilityService fieldAvailabilityService;
  private final FieldUsageBlockService fieldUsageBlockService;
  private final TransactionService transactionService;

  public DataSeeder(LeagueService leagueService, TeamService teamService,
                    FieldService fieldService, SeasonService seasonService, PlayerService playerService,
                    TournamentService tournamentService, GameService gameService,
                    FieldAvailabilityService fieldAvailabilityService, FieldUsageBlockService fieldUsageBlockService,
                    TransactionService transactionService) {
    this.leagueService = leagueService;
    this.teamService = teamService;
    this.fieldService = fieldService;
    this.seasonService = seasonService;
    this.playerService = playerService;
    this.tournamentService = tournamentService;
    this.gameService = gameService;
    this.fieldAvailabilityService = fieldAvailabilityService;
    this.fieldUsageBlockService = fieldUsageBlockService;
    this.transactionService = transactionService;
  }

  @Override
  public void run(String... args) {
    // Only seed if database is empty
    if (!leagueService.findAll().isEmpty()) {
      log.info("Database already contains data, skipping seed");
      return;
    }

    log.info("Seeding database with comprehensive historical data (2024-2026)...");

    // Create Leagues
    League soccerLeague = new League();
    soccerLeague.setName("Premier Soccer League");
    soccerLeague.setDescription("Competitive adult soccer league with teams from across the region");
    soccerLeague = leagueService.save(soccerLeague);

    League youthLeague = new League();
    youthLeague.setName("Youth Development League");
    youthLeague.setDescription("Youth soccer league for players aged 12-16");
    youthLeague = leagueService.save(youthLeague);

    League basketballLeague = new League();
    basketballLeague.setName("City Basketball Association");
    basketballLeague.setDescription("Indoor basketball league for amateur teams");
    basketballLeague = leagueService.save(basketballLeague);

    // Create Teams for Premier Soccer League
    Team thunderUnited = createTeam("Thunder United", "Mike Johnson", "mjohnson@example.com", soccerLeague);
    Team lightningFC = createTeam("Lightning FC", "Sarah Williams", "swilliams@example.com", soccerLeague);
    Team stormStrikers = createTeam("Storm Strikers", "David Brown", "dbrown@example.com", soccerLeague);
    Team phoenixRising = createTeam("Phoenix Rising", "Emily Davis", "edavis@example.com", soccerLeague);
    Team dragonWarriors = createTeam("Dragon Warriors", "James Wilson", "jwilson@example.com", soccerLeague);
    Team eagleKnights = createTeam("Eagle Knights", "Lisa Anderson", "landerson@example.com", soccerLeague);

    // Create Teams for Youth Development League
    Team juniorRockets = createTeam("Junior Rockets", "Tom Martinez", "tmartinez@example.com", youthLeague);
    Team youngLions = createTeam("Young Lions", "Mary Garcia", "mgarcia@example.com", youthLeague);
    Team futureStars = createTeam("Future Stars", "Robert Taylor", "rtaylor@example.com", youthLeague);
    Team risingChampions = createTeam("Rising Champions", "Jennifer Moore", "jmoore@example.com", youthLeague);

    // Create Teams for Basketball League
    Team downtownDunkers = createTeam("Downtown Dunkers", "Chris Jackson", "cjackson@example.com", basketballLeague);
    Team uptownShooters = createTeam("Uptown Shooters", "Amanda White", "awhite@example.com", basketballLeague);
    Team riversideRaptors = createTeam("Riverside Raptors", "Kevin Harris", "kharris@example.com", basketballLeague);
    Team lakesideLakers = createTeam("Lakeside Lakers", "Nicole Martin", "nmartin@example.com", basketballLeague);

    // Create Fields
    Field memorialStadium = createField("Memorial Stadium", "Downtown", "123 Main Street, City Center");
    Field riversidePark = createField("Riverside Park Field", "Riverside District", "456 River Road, Riverside");
    Field northComplex = createField("North Sports Complex", "North End", "789 North Avenue, North End");
    Field communityCourt = createField("Community Center Court", "Westside", "321 West Boulevard, Westside");
    Field centralArena = createField("Central Arena", "Downtown", "555 Central Plaza, City Center");

    // Seed hours of operation (availability) and dedicated blocks
    seedFieldHours(memorialStadium, 9, 21);
    seedFieldHours(riversidePark, 8, 20);
    seedFieldHours(northComplex, 8, 22);
    seedIndoorHours(communityCourt, 10, 22);
    seedIndoorHours(centralArena, 10, 23);

    seedLeagueBlocksWeekdays(memorialStadium, 18, 21);
    seedLeagueBlocksWeekdays(riversidePark, 17, 20);
    seedLeagueBlocksWeekdays(northComplex, 18, 21);
    seedPracticeBlocksWeekdays(communityCourt, 18, 20);
    seedTournamentBlocksWeekends(centralArena, 9, 18);

    // Create Rosters (same teams over the years)
    createSoccerRoster(thunderUnited);
    createSoccerRoster(lightningFC);
    createSoccerRoster(stormStrikers);
    createSoccerRoster(phoenixRising);
    createSoccerRoster(dragonWarriors);
    createSoccerRoster(eagleKnights);

    createYouthSoccerRoster(juniorRockets);
    createYouthSoccerRoster(youngLions);
    createYouthSoccerRoster(futureStars);
    createYouthSoccerRoster(risingChampions);

    createBasketballRoster(downtownDunkers);
    createBasketballRoster(uptownShooters);
    createBasketballRoster(riversideRaptors);
    createBasketballRoster(lakesideLakers);

    // Team lists for game generation
    List<Team> soccerTeams = List.of(thunderUnited, lightningFC, stormStrikers, phoenixRising, dragonWarriors, eagleKnights);
    List<Team> youthTeams = List.of(juniorRockets, youngLions, futureStars, risingChampions);
    List<Team> basketballTeams = List.of(downtownDunkers, uptownShooters, riversideRaptors, lakesideLakers);
    List<Field> outdoorFields = List.of(memorialStadium, riversidePark, northComplex);
    List<Field> indoorFields = List.of(communityCourt, centralArena);

    // ========== 2024 SEASONS (All Complete) ==========
    log.info("Creating 2024 historical seasons...");

    // Soccer Spring 2024 (COMPLETED)
    Season spring2024 = createSeason("Spring 2024", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 5, 31), soccerLeague);
    createGamesForSeason(spring2024, soccerTeams, outdoorFields, LocalTime.of(14, 0), true);

    // Soccer Summer 2024 (COMPLETED)
    Season summer2024 = createSeason("Summer 2024", LocalDate.of(2024, 6, 1), LocalDate.of(2024, 8, 31), soccerLeague);
    createGamesForSeason(summer2024, soccerTeams, outdoorFields, LocalTime.of(14, 0), true);

    // Soccer Fall 2024 (COMPLETED)
    Season fall2024 = createSeason("Fall 2024", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 11, 30), soccerLeague);
    createGamesForSeason(fall2024, soccerTeams, outdoorFields, LocalTime.of(14, 0), true);

    // Youth Spring 2024 (COMPLETED)
    Season youthSpring2024 = createSeason("Youth Spring 2024", LocalDate.of(2024, 3, 15), LocalDate.of(2024, 6, 15), youthLeague);
    createGamesForSeason(youthSpring2024, youthTeams, outdoorFields, LocalTime.of(10, 0), true);

    // Youth Fall 2024 (COMPLETED)
    Season youthFall2024 = createSeason("Youth Fall 2024", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 11, 30), youthLeague);
    createGamesForSeason(youthFall2024, youthTeams, outdoorFields, LocalTime.of(10, 0), true);

    // Basketball Winter 2024 (COMPLETED)
    Season winter2024 = createSeason("Winter League 2024", LocalDate.of(2024, 1, 15), LocalDate.of(2024, 3, 31), basketballLeague);
    createGamesForSeason(winter2024, basketballTeams, indoorFields, LocalTime.of(19, 0), true);

    // ========== 2025 SEASONS (All Complete) ==========
    log.info("Creating 2025 historical seasons...");

    // Soccer Spring 2025 (COMPLETED)
    Season spring2025 = createSeason("Spring 2025", LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 31), soccerLeague);
    createGamesForSeason(spring2025, soccerTeams, outdoorFields, LocalTime.of(14, 0), true);

    // Soccer Summer 2025 (COMPLETED)
    Season summer2025 = createSeason("Summer 2025", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), soccerLeague);
    createGamesForSeason(summer2025, soccerTeams, outdoorFields, LocalTime.of(14, 0), true);

    // Soccer Fall 2025 (COMPLETED)
    Season fall2025 = createSeason("Fall 2025", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30), soccerLeague);
    createGamesForSeason(fall2025, soccerTeams, outdoorFields, LocalTime.of(14, 0), true);

    // Youth Spring 2025 (COMPLETED)
    Season youthSpring2025 = createSeason("Youth Spring 2025", LocalDate.of(2025, 3, 15), LocalDate.of(2025, 6, 15), youthLeague);
    createGamesForSeason(youthSpring2025, youthTeams, outdoorFields, LocalTime.of(10, 0), true);

    // Youth Summer 2025 (COMPLETED)
    Season youthSummer2025 = createSeason("Youth Summer 2025", LocalDate.of(2025, 6, 20), LocalDate.of(2025, 9, 15), youthLeague);
    createGamesForSeason(youthSummer2025, youthTeams, outdoorFields, LocalTime.of(10, 0), true);

    // Youth Fall 2025 (COMPLETED)
    Season youthFall2025 = createSeason("Youth Fall 2025", LocalDate.of(2025, 9, 20), LocalDate.of(2025, 12, 15), youthLeague);
    createGamesForSeason(youthFall2025, youthTeams, outdoorFields, LocalTime.of(10, 0), true);

    // Basketball Winter 2025 (COMPLETED)
    Season winter2025 = createSeason("Winter League 2025", LocalDate.of(2025, 1, 15), LocalDate.of(2025, 3, 31), basketballLeague);
    createGamesForSeason(winter2025, basketballTeams, indoorFields, LocalTime.of(19, 0), true);

    // Basketball Fall 2025 (COMPLETED)
    Season fallBball2025 = createSeason("Fall League 2025", LocalDate.of(2025, 10, 1), LocalDate.of(2025, 12, 31), basketballLeague);
    createGamesForSeason(fallBball2025, basketballTeams, indoorFields, LocalTime.of(19, 0), true);

    // ========== 2026 SEASONS (Current/Upcoming) ==========
    log.info("Creating 2026 current seasons...");

    // Basketball Winter 2026 (IN PROGRESS - current date is Jan 10, 2026)
    Season winter2026 = createSeason("Winter League 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), basketballLeague);
    createGamesForSeason(winter2026, basketballTeams, indoorFields, LocalTime.of(19, 0), true);

    // Soccer Spring 2026 (UPCOMING)
    Season spring2026 = createSeason("Spring 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31), soccerLeague);
    createGamesForSeason(spring2026, soccerTeams, outdoorFields, LocalTime.of(14, 0), false);

    // Soccer Summer 2026 (UPCOMING)
    Season summer2026 = createSeason("Summer 2026", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), soccerLeague);
    createGamesForSeason(summer2026, soccerTeams, outdoorFields, LocalTime.of(14, 0), false);

    // Youth Spring 2026 (UPCOMING)
    Season youthSpring2026 = createSeason("Youth Spring 2026", LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 15), youthLeague);
    createGamesForSeason(youthSpring2026, youthTeams, outdoorFields, LocalTime.of(10, 0), false);

    // ========== TOURNAMENTS ==========
    log.info("Creating tournaments...");

    // Historical tournaments
    createTournament("Premier Cup 2024", "Championship 2024",
      Tournament.TournamentType.LEAGUE, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 15),
      soccerLeague, 8, LocalDate.of(2024, 6, 20), 50.0, "Memorial Stadium");

    createTournament("Premier Cup 2025", "Championship 2025",
      Tournament.TournamentType.LEAGUE, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 15),
      soccerLeague, 8, LocalDate.of(2025, 6, 20), 50.0, "Memorial Stadium");

    // Upcoming tournaments
    createTournament("Premier Cup 2026", "Annual championship for premier league teams",
      Tournament.TournamentType.LEAGUE, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15),
      soccerLeague, 8, LocalDate.of(2026, 6, 20), 50.0, "Memorial Stadium");

    createTournament("Open Spring Classic 2026", "Open tournament - all teams welcome",
      Tournament.TournamentType.OPEN, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10),
      null, 16, LocalDate.of(2026, 3, 25), 25.0, "Central Arena");

    createTournament("Youth Summer Showcase 2026", "Youth development tournament",
      Tournament.TournamentType.LEAGUE, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 17),
      youthLeague, 8, LocalDate.of(2026, 7, 1), 20.0, "Riverside Park Field");

    // Seed transactions with various statuses
    log.info("Seeding transactions...");
    seedTransactions();

    log.info("Database seeding completed successfully!");
    log.info("Created {} leagues, {} teams, {} players, {} fields, {} seasons, {} tournaments, {} games",
      leagueService.findAll().size(),
      teamService.findAll().size(),
      playerService.findAll().size(),
      fieldService.findAll().size(),
      seasonService.findAll().size(),
      tournamentService.findAll().size(),
      gameService.findAll().size());
  }

  private Team createTeam(String name, String coach, String email, League league) {
    Team team = new Team();
    team.setName(name);
    team.setCoach(coach);
    team.setContactEmail(email);
    team.setLeague(league);
    return teamService.save(team);
  }

  private void createSoccerRoster(Team team) {
    createPlayer(team.getName() + " Player 1", "Smith", 1, "Goalkeeper", team);
    createPlayer(team.getName() + " Player 2", "Mendez", 4, "Defender", team);
    createPlayer(team.getName() + " Player 3", "Johnson", 5, "Defender", team);
    createPlayer(team.getName() + " Player 4", "Chen", 6, "Midfielder", team);
    createPlayer(team.getName() + " Player 5", "Rodriguez", 7, "Midfielder", team);
    createPlayer(team.getName() + " Player 6", "Patterson", 8, "Midfielder", team);
    createPlayer(team.getName() + " Player 7", "Williams", 9, "Forward", team);
    createPlayer(team.getName() + " Player 8", "Brown", 10, "Forward", team);
    createPlayer(team.getName() + " Player 9", "Davis", 11, "Forward", team);
    createPlayer(team.getName() + " Player 10", "Garcia", 2, "Defender", team);
    createPlayer(team.getName() + " Player 11", "Martinez", 3, "Defender", team);
  }

  private void createYouthSoccerRoster(Team team) {
    createPlayer("Tyler", "Anderson", 1, "Goalkeeper", team);
    createPlayer("Jordan", "Taylor", 4, "Defender", team);
    createPlayer("Ryan", "Thomas", 5, "Defender", team);
    createPlayer("Chris", "Wilson", 7, "Midfielder", team);
    createPlayer("Brandon", "Moore", 8, "Midfielder", team);
    createPlayer("Kevin", "Jackson", 9, "Forward", team);
    createPlayer("Eric", "White", 10, "Forward", team);
    createPlayer("Steven", "Harris", 11, "Forward", team);
    createPlayer("Brian", "Martin", 2, "Defender", team);
  }

  private void createBasketballRoster(Team team) {
    createPlayer(team.getName() + " Player 1", "Cooper", 1, "Point Guard", team);
    createPlayer(team.getName() + " Player 2", "Edwards", 2, "Shooting Guard", team);
    createPlayer(team.getName() + " Player 3", "Collins", 3, "Small Forward", team);
    createPlayer(team.getName() + " Player 4", "Stewart", 4, "Power Forward", team);
    createPlayer(team.getName() + " Player 5", "Sanchez", 5, "Center", team);
    createPlayer(team.getName() + " Player 6", "Morris", 6, "Shooting Guard", team);
    createPlayer(team.getName() + " Player 7", "Rogers", 7, "Small Forward", team);
    createPlayer(team.getName() + " Player 8", "Peterson", 8, "Power Forward", team);
    createPlayer(team.getName() + " Player 9", "Gray", 9, "Center", team);
    createPlayer(team.getName() + " Player 10", "Ramirez", 11, "Point Guard", team);
  }

  private void createPlayer(String firstName, String lastName, Integer jerseyNumber, String position, Team team) {
    // Prevent duplicate players based on name
    if (playerService.findByFirstNameAndLastName(firstName, lastName).isPresent()) {
      return;
    }
    Player player = new Player();
    player.setFirstName(faker.name().firstName());
    player.setLastName(faker.name().lastName());
    player.setJerseyNumber(jerseyNumber);
    player.setPosition(position);
    player.setTeam(team);
    playerService.save(player);
  }

  private void createTournament(String name, String description, Tournament.TournamentType type,
                                LocalDate startDate, LocalDate endDate, League league, Integer maxTeams,
                                LocalDate registrationDeadline, Double entryFee, String location) {
    Tournament tournament = new Tournament();
    tournament.setName(name);
    tournament.setDescription(description);
    tournament.setType(type);
    tournament.setStartDate(startDate);
    tournament.setEndDate(endDate);
    tournament.setLeague(league);
    tournament.setMaxTeams(maxTeams);
    tournament.setRegistrationDeadline(registrationDeadline);
    tournament.setEntryFee(entryFee);
    tournament.setLocation(location);
    tournament.setStatus(Tournament.TournamentStatus.REGISTRATION);
    tournamentService.save(tournament);
  }

  private Field createField(String name, String location, String address) {
    Field field = new Field();
    field.setName(name);
    field.setLocation(location);
    field.setAddress(address);
    return fieldService.save(field);
  }

  private Season createSeason(String name, LocalDate startDate, LocalDate endDate, League league) {
    Season season = new Season();
    season.setName(name);
    season.setStartDate(startDate);
    season.setEndDate(endDate);
    season.setLeague(league);
    return seasonService.save(season);
  }

  /**
   * Create a round-robin schedule of games for a season
   * Games are only scheduled on days/times when fields are actually available
   */
  private void createGamesForSeason(Season season, List<Team> teams, List<Field> fields,
                                    LocalTime gameTime, boolean includePastGames) {
    LocalDate currentDate = LocalDate.now();
    LocalDate startDate = season.getStartDate();
    int teamCount = teams.size();
    int fieldIndex = 0;

    // Generate round-robin schedule (each team plays each other once)
    for (int week = 0; week < teamCount - 1; week++) {
      LocalDate gameDate = startDate.plusWeeks(week);

      // Skip Sundays - fields are configured closed on Sunday
      if (gameDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
        week++;
        gameDate = startDate.plusWeeks(week);
      }

      // Determine game status based on date
      Game.GameStatus status;
      if (gameDate.isBefore(currentDate)) {
        if (!includePastGames) continue;
        status = Game.GameStatus.COMPLETED;
      } else if (gameDate.isEqual(currentDate)) {
        status = Game.GameStatus.IN_PROGRESS;
      } else {
        status = Game.GameStatus.SCHEDULED;
      }

      // Create matchups for this week
      for (int match = 0; match < teamCount / 2; match++) {
        int home = (week + match) % (teamCount - 1);
        int away = (teamCount - 1 - match + week) % (teamCount - 1);

        // Last team stays in place, others rotate
        if (match == 0) {
          away = teamCount - 1;
        }

        Team homeTeam = teams.get(home);
        Team awayTeam = teams.get(away);
        Field field = fields.get(fieldIndex % fields.size());
        fieldIndex++;

        // Create game at the specified time
        LocalDateTime gameDateTime = LocalDateTime.of(gameDate, gameTime.plusHours(match * 2));

        Game game = new Game();
        game.setGameDate(gameDateTime);
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setField(field);
        game.setSeason(season);
        game.setStatus(status);

        // Add scores for completed games
        if (status == Game.GameStatus.COMPLETED) {
          game.setHomeScore((int) (Math.random() * 5));
          game.setAwayScore((int) (Math.random() * 5));
        } else if (status == Game.GameStatus.IN_PROGRESS) {
          game.setHomeScore((int) (Math.random() * 3));
          game.setAwayScore((int) (Math.random() * 3));
        }

        gameService.save(game);
      }
    }
  }

  private void seedFieldHours(Field field, int openHour, int closeHour) {
    // Mon-Fri: openHour-closeHour, Sat: openHour- (closeHour-2), Sun: closed
    for (DayOfWeek day : DayOfWeek.values()) {
      switch (day) {
        case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY ->
          fieldAvailabilityService.save(new FieldAvailability(field, day,
            LocalTime.of(openHour, 0), LocalTime.of(closeHour, 0)));
        case SATURDAY -> fieldAvailabilityService.save(new FieldAvailability(field, day,
          LocalTime.of(openHour, 0), LocalTime.of(Math.max(openHour, closeHour - 2), 0)));
        case SUNDAY -> { /* closed */ }
      }
    }
  }

  private void seedIndoorHours(Field field, int openHour, int closeHour) {
    // Indoor: open all days with same hours
    for (DayOfWeek day : DayOfWeek.values()) {
      fieldAvailabilityService.save(new FieldAvailability(field, day,
        LocalTime.of(openHour, 0), LocalTime.of(closeHour, 0)));
    }
  }

  private void seedLeagueBlocksWeekdays(Field field, int startHour, int endHour) {
    for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
      fieldUsageBlockService.save(new FieldUsageBlock(field, day, FieldUsageBlock.UsageType.LEAGUE,
        LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), "League play"));
    }
  }

  private void seedPracticeBlocksWeekdays(Field field, int startHour, int endHour) {
    for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY}) {
      fieldUsageBlockService.save(new FieldUsageBlock(field, day, FieldUsageBlock.UsageType.PRACTICE,
        LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), "Team practices"));
    }
  }

  private void seedTournamentBlocksWeekends(Field field, int startHour, int endHour) {
    // Only Saturday for tournament blocks - Sunday is left flexible by user
    fieldUsageBlockService.save(new FieldUsageBlock(field, DayOfWeek.SATURDAY, FieldUsageBlock.UsageType.TOURNAMENT,
      LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), "Tournament play"));
  }

  private void seedTransactions() {
    try {
      List<Player> players = playerService.findAll();
      if (players.isEmpty()) {
        log.warn("No players found to seed transactions for.");
        return;
      }
      // PENDING PAYMENTS - Recent invoices
      Transaction pending1 = new Transaction();
      pending1.setPartyType(Transaction.PartyType.TEAM);
      pending1.setPartyId(1L); // Thunder United
      pending1.setCategory(Transaction.Category.INVOICE);
      pending1.setDate(LocalDate.now().minusDays(5));
      pending1.setAmount(250.00);
      pending1.setStatus(Transaction.Status.PENDING);
      pending1.setNotes("League registration fee - Winter 2026");
      transactionService.save(pending1);

      Transaction pending2 = new Transaction();
      pending2.setPartyType(Transaction.PartyType.TEAM);
      pending2.setPartyId(2L); // Lightning FC
      pending2.setCategory(Transaction.Category.INVOICE);
      pending2.setDate(LocalDate.now().minusDays(3));
      pending2.setAmount(200.00);
      pending2.setStatus(Transaction.Status.PENDING);
      pending2.setNotes("Tournament entry fee - Open Spring Classic");
      transactionService.save(pending2);

      Transaction pending3 = new Transaction();
      pending3.setPartyType(Transaction.PartyType.TEAM);
      pending3.setPartyId(3L); // Storm Strikers
      pending3.setCategory(Transaction.Category.INVOICE);
      pending3.setDate(LocalDate.now().minusDays(7));
      pending3.setAmount(175.00);
      pending3.setStatus(Transaction.Status.PENDING);
      pending3.setNotes("Facility rental fee - Spring season");
      transactionService.save(pending3);

      // Player PENDING
      Transaction playerPending = new Transaction();
      playerPending.setPartyType(Transaction.PartyType.PLAYER);
      playerPending.setPartyId(players.get(0).getId());
      playerPending.setCategory(Transaction.Category.INVOICE);
      playerPending.setDate(LocalDate.now().minusDays(10));
      playerPending.setAmount(50.00);
      playerPending.setStatus(Transaction.Status.PENDING);
      playerPending.setNotes("Player registration fee");
      transactionService.save(playerPending);


      // PAID PAYMENTS - Completed transactions
      Transaction paid1 = new Transaction();
      paid1.setPartyType(Transaction.PartyType.TEAM);
      paid1.setPartyId(4L); // Phoenix Rising
      paid1.setCategory(Transaction.Category.PAYMENT);
      paid1.setDate(LocalDate.now().minusDays(30));
      paid1.setAmount(250.00);
      paid1.setStatus(Transaction.Status.PAID);
      paid1.setNotes("League registration fee - Winter 2026 PAID");
      transactionService.save(paid1);

      Transaction paid2 = new Transaction();
      paid2.setPartyType(Transaction.PartyType.TEAM);
      paid2.setPartyId(5L); // Dragon Warriors
      paid2.setCategory(Transaction.Category.PAYMENT);
      paid2.setDate(LocalDate.now().minusDays(20));
      paid2.setAmount(150.00);
      paid2.setStatus(Transaction.Status.PAID);
      paid2.setNotes("Equipment fee - PAID");
      transactionService.save(paid2);

      Transaction paid3 = new Transaction();
      paid3.setPartyType(Transaction.PartyType.TEAM);
      paid3.setPartyId(6L); // Eagle Knights
      paid3.setCategory(Transaction.Category.PAYMENT);
      paid3.setDate(LocalDate.now().minusDays(15));
      paid3.setAmount(200.00);
      paid3.setStatus(Transaction.Status.PAID);
      paid3.setNotes("Tournament entry fee - Premier Cup PAID");
      transactionService.save(paid3);

      Transaction paid4 = new Transaction();
      paid4.setPartyType(Transaction.PartyType.TEAM);
      paid4.setPartyId(1L); // Thunder United
      paid4.setCategory(Transaction.Category.PAYMENT);
      paid4.setDate(LocalDate.now().minusDays(10));
      paid4.setAmount(100.00);
      paid4.setStatus(Transaction.Status.PAID);
      paid4.setNotes("Partial payment for Spring season - PAID");
      transactionService.save(paid4);

      // Player PAID
      Transaction playerPaid = new Transaction();
      playerPaid.setPartyType(Transaction.PartyType.PLAYER);
      playerPaid.setPartyId(players.get(1).getId());
      playerPaid.setCategory(Transaction.Category.PAYMENT);
      playerPaid.setDate(LocalDate.now().minusDays(40));
      playerPaid.setAmount(75.00);
      playerPaid.setStatus(Transaction.Status.PAID);
      playerPaid.setNotes("Player uniform fee - PAID");
      transactionService.save(playerPaid);

      // OVERDUE PAYMENTS - Past due invoices
      Transaction overdue1 = new Transaction();
      overdue1.setPartyType(Transaction.PartyType.TEAM);
      overdue1.setPartyId(2L); // Lightning FC
      overdue1.setCategory(Transaction.Category.INVOICE);
      overdue1.setDate(LocalDate.now().minusDays(60)); // 2 months old
      overdue1.setAmount(300.00);
      overdue1.setStatus(Transaction.Status.OVERDUE);
      overdue1.setNotes("Fall 2025 season fee - OVERDUE");
      transactionService.save(overdue1);

      Transaction overdue2 = new Transaction();
      overdue2.setPartyType(Transaction.PartyType.TEAM);
      overdue2.setPartyId(4L); // Phoenix Rising
      overdue2.setCategory(Transaction.Category.INVOICE);
      overdue2.setDate(LocalDate.now().minusDays(45)); // 1.5 months old
      overdue2.setAmount(225.00);
      overdue2.setStatus(Transaction.Status.OVERDUE);
      overdue2.setNotes("Winter training facility fee - OVERDUE");
      transactionService.save(overdue2);

      Transaction overdue3 = new Transaction();
      overdue3.setPartyType(Transaction.PartyType.TEAM);
      overdue3.setPartyId(5L); // Dragon Warriors
      overdue3.setCategory(Transaction.Category.INVOICE);
      overdue3.setDate(LocalDate.now().minusDays(90)); // 3 months old
      overdue3.setAmount(175.00);
      overdue3.setStatus(Transaction.Status.OVERDUE);
      overdue3.setNotes("Fall 2025 tournament entry - OVERDUE");
      transactionService.save(overdue3);

      // Player OVERDUE
      Transaction playerOverdue = new Transaction();
      playerOverdue.setPartyType(Transaction.PartyType.PLAYER);
      playerOverdue.setPartyId(players.get(2).getId());
      playerOverdue.setCategory(Transaction.Category.INVOICE);
      playerOverdue.setDate(LocalDate.now().minusDays(50));
      playerOverdue.setAmount(60.00);
      playerOverdue.setStatus(Transaction.Status.OVERDUE);
      playerOverdue.setNotes("Player fine - OVERDUE");
      transactionService.save(playerOverdue);

      log.info("Transactions seeded with PENDING, PAID, and OVERDUE statuses");
    } catch (Exception e) {
      log.warn("Could not seed transactions: {}", e.getMessage());
    }
  }
}
