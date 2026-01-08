package com.scheduleengine.service;

import com.scheduleengine.domain.Game;
import com.scheduleengine.domain.League;
import com.scheduleengine.domain.Season;
import com.scheduleengine.domain.Team;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ScheduleGeneratorService {

    public static class Match {
        private final Team home;
        private final Team away;
        public Match(Team home, Team away) {
            this.home = home;
            this.away = away;
        }
        public Team getHome() { return home; }
        public Team getAway() { return away; }
    }

    public static class Round {
        private final int roundNumber;
        private final List<Match> matches;
        public Round(int roundNumber, List<Match> matches) {
            this.roundNumber = roundNumber;
            this.matches = matches;
        }
        public int getRoundNumber() { return roundNumber; }
        public List<Match> getMatches() { return matches; }
    }

    private final TeamService teamService;
    private final GameService gameService;

    public ScheduleGeneratorService(TeamService teamService, GameService gameService) {
        this.teamService = teamService;
        this.gameService = gameService;
    }

    /**
     * Generate a single round-robin schedule for the league attached to the given season.
     * Uses the standard "circle method". If odd team count, a BYE is inserted and any match with BYE is skipped.
     * No Game entities are created; this only returns pairings by round.
     */
    public List<Round> generateRoundRobin(League league, Season season) {
        if (league == null) throw new IllegalArgumentException("league is required");
        List<Team> teams = new ArrayList<>(teamService.findByLeagueId(league.getId()));
        List<Round> rounds = new ArrayList<>();
        if (teams.isEmpty() || teams.size() == 1) return rounds; // nothing to schedule

        // If odd, add a null as BYE
        if (teams.size() % 2 == 1) {
            teams.add(null);
        }
        int n = teams.size();
        int pivotCount = n - 1; // number of rounds in single RR

        // Prepare rotation list (first team fixed, rotate the rest)
        List<Team> rotation = new ArrayList<>(teams);

        for (int roundIdx = 0; roundIdx < pivotCount; roundIdx++) {
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < n / 2; i++) {
                Team t1 = rotation.get(i);
                Team t2 = rotation.get(n - 1 - i);
                if (t1 == null || t2 == null) {
                    // BYE week, skip making a match
                    continue;
                }
                // Alternate home/away across rounds for balance
                if (roundIdx % 2 == 0) {
                    matches.add(new Match(t1, t2));
                } else {
                    matches.add(new Match(t2, t1));
                }
            }
            rounds.add(new Round(roundIdx + 1, matches));

            // Rotate (keep first element fixed, rotate the rest clockwise)
            Team fixed = rotation.get(0);
            List<Team> tail = new ArrayList<>(rotation.subList(1, rotation.size()));
            Collections.rotate(tail, 1);
            rotation.clear();
            rotation.add(fixed);
            rotation.addAll(tail);
        }

        return rounds;
    }

    /**
     * Persist a generated schedule as Game rows for the given season. If overwrite is true, existing season games are deleted first.
     * Dates are assigned sequentially starting today at 6pm, one round per day.
     */
    public List<Game> generateAndPersist(League league, Season season, boolean overwrite) {
        if (overwrite) {
            gameService.deleteBySeasonId(season.getId());
        }
        List<Round> rounds = generateRoundRobin(league, season);
        List<Game> created = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0);
        for (Round r : rounds) {
            LocalDateTime roundDate = base.plusDays(r.getRoundNumber() - 1);
            for (Match m : r.getMatches()) {
                Game g = new Game();
                g.setSeason(season);
                g.setHomeTeam(m.getHome());
                g.setAwayTeam(m.getAway());
                g.setGameDate(roundDate);
                g.setStatus(Game.GameStatus.SCHEDULED);
                created.add(gameService.save(g));
            }
        }
        return created;
    }
}
