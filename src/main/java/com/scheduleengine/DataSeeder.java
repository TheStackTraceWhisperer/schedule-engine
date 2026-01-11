package com.scheduleengine;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.player.domain.Player;
import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.game.domain.Game;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.tournament.service.TournamentService;
import com.scheduleengine.game.service.GameService;
import com.scheduleengine.field.domain.FieldAvailability;
import com.scheduleengine.field.domain.FieldUsageBlock;
import com.scheduleengine.field.service.FieldAvailabilityService;
import com.scheduleengine.field.service.FieldUsageBlockService;
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

    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final PlayerService playerService;
    private final TournamentService tournamentService;
    private final GameService gameService;
    private final FieldAvailabilityService fieldAvailabilityService;
    private final FieldUsageBlockService fieldUsageBlockService;

    public DataSeeder(LeagueService leagueService, TeamService teamService,
                     FieldService fieldService, SeasonService seasonService, PlayerService playerService,
                     TournamentService tournamentService, GameService gameService,
                     FieldAvailabilityService fieldAvailabilityService, FieldUsageBlockService fieldUsageBlockService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.playerService = playerService;
        this.tournamentService = tournamentService;
        this.gameService = gameService;
        this.fieldAvailabilityService = fieldAvailabilityService;
        this.fieldUsageBlockService = fieldUsageBlockService;
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
        createPlayer("John", "Smith", 1, "Goalkeeper", team);
        createPlayer("Carlos", "Mendez", 4, "Defender", team);
        createPlayer("Alex", "Johnson", 5, "Defender", team);
        createPlayer("Michael", "Chen", 6, "Midfielder", team);
        createPlayer("David", "Rodriguez", 7, "Midfielder", team);
        createPlayer("James", "Patterson", 8, "Midfielder", team);
        createPlayer("Samuel", "Williams", 9, "Forward", team);
        createPlayer("Marcus", "Brown", 10, "Forward", team);
        createPlayer("Lucas", "Davis", 11, "Forward", team);
        createPlayer("Oliver", "Garcia", 2, "Defender", team);
        createPlayer("Ethan", "Martinez", 3, "Defender", team);
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
        createPlayer("Anthony", "Cooper", 1, "Point Guard", team);
        createPlayer("Kyle", "Edwards", 2, "Shooting Guard", team);
        createPlayer("Derek", "Collins", 3, "Small Forward", team);
        createPlayer("Jason", "Stewart", 4, "Power Forward", team);
        createPlayer("Patrick", "Sanchez", 5, "Center", team);
        createPlayer("Matthew", "Morris", 6, "Shooting Guard", team);
        createPlayer("Andrew", "Rogers", 7, "Small Forward", team);
        createPlayer("Daniel", "Peterson", 8, "Power Forward", team);
        createPlayer("Christopher", "Gray", 9, "Center", team);
        createPlayer("Joshua", "Ramirez", 11, "Point Guard", team);
    }

    private void createPlayer(String firstName, String lastName, Integer jerseyNumber, String position, Team team) {
        Player player = new Player();
        player.setFirstName(firstName);
        player.setLastName(lastName);
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
                case SATURDAY ->
                    fieldAvailabilityService.save(new FieldAvailability(field, day,
                        LocalTime.of(openHour, 0), LocalTime.of(Math.max(openHour, closeHour - 2), 0)));
                case SUNDAY -> { /* closed */ }
            }
        }
    }

    private void seedIndoorHours(Field field, int openHour, int closeHour) {
        // Indoor: open all days with slightly shorter Sunday
        for (DayOfWeek day : DayOfWeek.values()) {
            int sundayClose = Math.max(openHour + 6, closeHour - 2);
            int effectiveClose = day == DayOfWeek.SUNDAY ? sundayClose : closeHour;
            fieldAvailabilityService.save(new FieldAvailability(field, day,
                LocalTime.of(openHour, 0), LocalTime.of(effectiveClose, 0)));
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
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}) {
            fieldUsageBlockService.save(new FieldUsageBlock(field, day, FieldUsageBlock.UsageType.TOURNAMENT,
                LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), "Tournament play"));
        }
    }
}
