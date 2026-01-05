package com.scheduleengine.controller;

import com.scheduleengine.domain.League;
import com.scheduleengine.service.LeagueService;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/leagues")
public class LeagueController {
    
    private final LeagueService leagueService;
    
    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }
    
    @Get
    @View("leagues/list")
    public Map<String, Object> list() {
        Map<String, Object> model = new HashMap<>();
        model.put("leagues", leagueService.findAll());
        return model;
    }
    
    @Get("/new")
    @View("leagues/form")
    public Map<String, Object> newLeague() {
        Map<String, Object> model = new HashMap<>();
        model.put("league", new League());
        model.put("action", "/leagues");
        return model;
    }
    
    @Post
    public String create(@Body League league) {
        leagueService.save(league);
        return "redirect:/leagues";
    }
    
    @Get("/{id}/edit")
    @View("leagues/form")
    public Map<String, Object> edit(Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("league", leagueService.findById(id).orElseThrow());
        model.put("action", "/leagues/" + id);
        return model;
    }
    
    @Post("/{id}")
    public String update(Long id, @Body League league) {
        leagueService.update(id, league);
        return "redirect:/leagues";
    }
    
    @Get("/{id}/delete")
    public String delete(Long id) {
        leagueService.deleteById(id);
        return "redirect:/leagues";
    }
}
