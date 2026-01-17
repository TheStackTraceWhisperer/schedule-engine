package com.scheduleengine.tournament.domain;

import com.scheduleengine.league.domain.League;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "tournaments")
public class Tournament {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TournamentType type;

  @NotNull
  @Column(nullable = false, name = "start_date")
  private LocalDate startDate;

  @NotNull
  @Column(nullable = false, name = "end_date")
  private LocalDate endDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "league_id")
  private League league; // null for open tournaments

  @Column(name = "max_teams")
  private Integer maxTeams;

  @Column(name = "registration_deadline")
  private LocalDate registrationDeadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TournamentStatus status = TournamentStatus.DRAFT;

  @Column(name = "entry_fee")
  private Double entryFee;

  @Column(length = 500)
  private String location;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  // Getters and Setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TournamentType getType() {
    return type;
  }

  public void setType(TournamentType type) {
    this.type = type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public League getLeague() {
    return league;
  }

  public void setLeague(League league) {
    this.league = league;
  }

  public Integer getMaxTeams() {
    return maxTeams;
  }

  public void setMaxTeams(Integer maxTeams) {
    this.maxTeams = maxTeams;
  }

  public LocalDate getRegistrationDeadline() {
    return registrationDeadline;
  }

  public void setRegistrationDeadline(LocalDate registrationDeadline) {
    this.registrationDeadline = registrationDeadline;
  }

  public TournamentStatus getStatus() {
    return status;
  }

  public void setStatus(TournamentStatus status) {
    this.status = status;
  }

  public Double getEntryFee() {
    return entryFee;
  }

  public void setEntryFee(Double entryFee) {
    this.entryFee = entryFee;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public enum TournamentType {
    LEAGUE("League Only - Restricted to teams within a specific league"),
    OPEN("Open - Any team can register"),
    INVITATIONAL("Invitational - Invitation only by tournament organizer");

    private final String description;

    TournamentType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum TournamentStatus {
    DRAFT,          // Being planned
    REGISTRATION,   // Open for registration
    FULL,          // Max teams reached
    IN_PROGRESS,   // Tournament underway
    COMPLETED,     // Finished
    CANCELLED      // Cancelled
  }
}
