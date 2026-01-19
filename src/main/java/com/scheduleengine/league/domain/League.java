package com.scheduleengine.league.domain;

import com.scheduleengine.season.domain.Season;
import com.scheduleengine.team.domain.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leagues")
public class League {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Team> teams = new ArrayList<>();

  @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Season> seasons = new ArrayList<>();

  @Column(name = "icon_name")
  private String iconName;

  @Column(name = "icon_bg_color")
  private String iconBackgroundColor;

  @Column(name = "icon_glyph_color")
  private String iconGlyphColor;

  public League() {
  }

  public League(String name) {
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Team> getTeams() {
    return teams;
  }

  public void setTeams(List<Team> teams) {
    this.teams = teams;
  }

  public List<Season> getSeasons() {
    return seasons;
  }

  public void setSeasons(List<Season> seasons) {
    this.seasons = seasons;
  }

  public String getIconName() {
    return iconName;
  }

  public void setIconName(String iconName) {
    this.iconName = iconName;
  }

  public String getIconBackgroundColor() {
    return iconBackgroundColor;
  }

  public void setIconBackgroundColor(String iconBackgroundColor) {
    this.iconBackgroundColor = iconBackgroundColor;
  }

  public String getIconGlyphColor() {
    return iconGlyphColor;
  }

  public void setIconGlyphColor(String iconGlyphColor) {
    this.iconGlyphColor = iconGlyphColor;
  }
}
