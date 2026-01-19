package com.scheduleengine.field.domain;

import com.scheduleengine.game.domain.Game;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fields")
public class Field {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  private String location;

  private String address;

  @Column(length = 1000)
  private String facilities;

  @OneToMany(mappedBy = "field", cascade = CascadeType.ALL)
  private List<Game> games = new ArrayList<>();

  @Column(name = "icon_name")
  private String iconName;

  @Column(name = "icon_bg_color")
  private String iconBackgroundColor;

  @Column(name = "icon_glyph_color")
  private String iconGlyphColor;

  public Field() {
  }

  public Field(String name) {
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

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getFacilities() {
    return facilities;
  }

  public void setFacilities(String facilities) {
    this.facilities = facilities;
  }

  public List<Game> getGames() {
    return games;
  }

  public void setGames(List<Game> games) {
    this.games = games;
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
