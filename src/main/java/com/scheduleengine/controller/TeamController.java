package com.scheduleengine.controller;

import com.scheduleengine.domain.Team;
import com.scheduleengine.service.LeagueService;
import com.scheduleengine.service.TeamService;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/teams")
public class TeamController {
    
    private final TeamService teamService;
    private final LeagueService leagueService;
    
    public TeamController(TeamService teamService, LeagueService leagueService) {
        this.teamService = teamService;
        this.leagueService = leagueService;
    }
    
    @Get
    @View("teams/list")
    public Map<String, Object> list() {
        Map<String, Object> model = new HashMap<>();
        model.put("teams", teamService.findAll());
        return model;
    }
    
    @Get("/new")
    @View("teams/form")
    public Map<String, Object> newTeam() {
        Map<String, Object> model = new HashMap<>();
        model.put("team", new Team());
        model.put("leagues", leagueService.findAll());
        model.put("action", "/teams");
        return model;
    }
    
    @Post
    public String create(@Body Team team) {
        teamService.save(team);
        return "redirect:/teams";
    }
    
    @Get("/{id}/edit")
    @View("teams/form")
    public Map<String, Object> edit(Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("team", teamService.findById(id).orElseThrow());
        model.put("leagues", leagueService.findAll());
        model.put("action", "/teams/" + id);
        return model;
    }
    
    @Post("/{id}")
    public String update(Long id, @Body Team team) {
        teamService.update(id, team);
        return "redirect:/teams";
    }
    
    @Get("/{id}/delete")
    public String delete(Long id) {
        teamService.deleteById(id);
        return "redirect:/teams";
    }
}
