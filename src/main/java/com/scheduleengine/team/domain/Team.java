package com.scheduleengine.team.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.scheduleengine.league.domain.League;
import com.scheduleengine.game.domain.Game;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    private String coach;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "league_id")
    private League league;
    
    @OneToMany(mappedBy = "homeTeam", cascade = CascadeType.ALL)
    private List<Game> homeGames = new ArrayList<>();
    
    @OneToMany(mappedBy = "awayTeam", cascade = CascadeType.ALL)
    private List<Game> awayGames = new ArrayList<>();
    
    public Team() {
    }
    
    public Team(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCoach() {
        return coach;
    }
    
    public void setCoach(String coach) {
        this.coach = coach;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public League getLeague() {
        return league;
    }
    
    public void setLeague(League league) {
        this.league = league;
    }
    
    public List<Game> getHomeGames() {
        return homeGames;
    }
    
    public void setHomeGames(List<Game> homeGames) {
        this.homeGames = homeGames;
    }
    
    public List<Game> getAwayGames() {
        return awayGames;
    }
    
    public void setAwayGames(List<Game> awayGames) {
        this.awayGames = awayGames;
    }
}
