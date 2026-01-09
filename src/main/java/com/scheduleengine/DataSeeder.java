package com.scheduleengine;

import com.scheduleengine.field.domain.Field;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.season.domain.Season;
import com.scheduleengine.team.domain.Team;
import com.scheduleengine.player.domain.Player;
import com.scheduleengine.tournament.domain.Tournament;
import com.scheduleengine.field.service.FieldService;
import com.scheduleengine.league.service.LeagueService;
import com.scheduleengine.season.service.SeasonService;
import com.scheduleengine.team.service.TeamService;
import com.scheduleengine.player.service.PlayerService;
import com.scheduleengine.tournament.service.TournamentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final PlayerService playerService;
    private final TournamentService tournamentService;

    public DataSeeder(LeagueService leagueService, TeamService teamService,
                     FieldService fieldService, SeasonService seasonService, PlayerService playerService,
                     TournamentService tournamentService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.playerService = playerService;
        this.tournamentService = tournamentService;
    }

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (!leagueService.findAll().isEmpty()) {
            log.info("Database already contains data, skipping seed");
            return;
        }

        log.info("Seeding database with demo data...");

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
        createField("Memorial Stadium", "Downtown", "123 Main Street, City Center");
        createField("Riverside Park Field", "Riverside District", "456 River Road, Riverside");
        createField("North Sports Complex", "North End", "789 North Avenue, North End");
        createField("Community Center Court", "Westside", "321 West Boulevard, Westside");
        createField("Central Arena", "Downtown", "555 Central Plaza, City Center");

        // Create Seasons
        createSeason("Spring 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31), soccerLeague);
        createSeason("Summer 2026", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), soccerLeague);
        createSeason("Youth Spring 2026", LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 15), youthLeague);
        createSeason("Winter League 2026", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 31), basketballLeague);

        // Create Rosters for Soccer Teams
        createSoccerRoster(thunderUnited);
        createSoccerRoster(lightningFC);
        createSoccerRoster(stormStrikers);
        createSoccerRoster(phoenixRising);
        createSoccerRoster(dragonWarriors);
        createSoccerRoster(eagleKnights);

        // Create Rosters for Youth Soccer Teams
        createYouthSoccerRoster(juniorRockets);
        createYouthSoccerRoster(youngLions);
        createYouthSoccerRoster(futureStars);
        createYouthSoccerRoster(risingChampions);

        // Create Rosters for Basketball Teams
        createBasketballRoster(downtownDunkers);
        createBasketballRoster(uptownShooters);
        createBasketballRoster(riversideRaptors);
        createBasketballRoster(lakesideLakers);

        // Create Tournaments
        createTournament("Premier Cup 2026", "Annual championship for premier league teams",
                Tournament.TournamentType.LEAGUE_SCOPED, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15),
                soccerLeague, 8, LocalDate.of(2026, 6, 20), 50.0, "Memorial Stadium");

        createTournament("Open Spring Classic", "Open tournament - all teams welcome",
                Tournament.TournamentType.OPEN, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10),
                null, 16, LocalDate.of(2026, 3, 25), 25.0, "Central Arena");

        createTournament("Elite Invitational", "Invitation only showcase tournament",
                Tournament.TournamentType.INVITATIONAL, LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 22),
                null, 8, LocalDate.of(2026, 8, 1), 100.0, "North Sports Complex");

        createTournament("Youth Summer Showcase", "Youth development tournament",
                Tournament.TournamentType.LEAGUE_SCOPED, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 17),
                youthLeague, 8, LocalDate.of(2026, 7, 1), 20.0, "Riverside Park Field");

        log.info("Database seeding completed successfully!");
        log.info("Created {} leagues, {} teams, {} players, {} fields, {} seasons, {} tournaments",
                leagueService.findAll().size(),
                teamService.findAll().size(),
                playerService.findAll().size(),
                fieldService.findAll().size(),
                seasonService.findAll().size(),
                tournamentService.findAll().size());
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

    private void createField(String name, String location, String address) {
        Field field = new Field();
        field.setName(name);
        field.setLocation(location);
        field.setAddress(address);
        fieldService.save(field);
    }

    private void createSeason(String name, LocalDate startDate, LocalDate endDate, League league) {
        Season season = new Season();
        season.setName(name);
        season.setStartDate(startDate);
        season.setEndDate(endDate);
        season.setLeague(league);
        seasonService.save(season);
    }
}

