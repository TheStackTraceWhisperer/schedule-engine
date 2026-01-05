package com.scheduleengine.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "game_date", nullable = false)
    private LocalDateTime gameDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private Field field;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
    
    @Column(name = "home_score")
    private Integer homeScore;
    
    @Column(name = "away_score")
    private Integer awayScore;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GameStatus status = GameStatus.SCHEDULED;
    
    private String notes;
    
    public Game() {
    }
    
    public Game(LocalDateTime gameDate, Team homeTeam, Team awayTeam) {
        this.gameDate = gameDate;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getGameDate() {
        return gameDate;
    }
    
    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }
    
    public Team getHomeTeam() {
        return homeTeam;
    }
    
    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }
    
    public Team getAwayTeam() {
        return awayTeam;
    }
    
    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }
    
    public Field getField() {
        return field;
    }
    
    public void setField(Field field) {
        this.field = field;
    }
    
    public Season getSeason() {
        return season;
    }
    
    public void setSeason(Season season) {
        this.season = season;
    }
    
    public Integer getHomeScore() {
        return homeScore;
    }
    
    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }
    
    public Integer getAwayScore() {
        return awayScore;
    }
    
    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public void setStatus(GameStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public enum GameStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        POSTPONED
    }
}
