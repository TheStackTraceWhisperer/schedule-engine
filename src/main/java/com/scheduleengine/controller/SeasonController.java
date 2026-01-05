package com.scheduleengine.controller;

import com.scheduleengine.domain.Season;
import com.scheduleengine.service.LeagueService;
import com.scheduleengine.service.SeasonService;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/seasons")
public class SeasonController {
    
    private final SeasonService seasonService;
    private final LeagueService leagueService;
    
    public SeasonController(SeasonService seasonService, LeagueService leagueService) {
        this.seasonService = seasonService;
        this.leagueService = leagueService;
    }
    
    @Get
    @View("seasons/list")
    public Map<String, Object> list() {
        Map<String, Object> model = new HashMap<>();
        model.put("seasons", seasonService.findAll());
        return model;
    }
    
    @Get("/new")
    @View("seasons/form")
    public Map<String, Object> newSeason() {
        Map<String, Object> model = new HashMap<>();
        model.put("season", new Season());
        model.put("leagues", leagueService.findAll());
        model.put("action", "/seasons");
        return model;
    }
    
    @Post
    public String create(@Body Season season) {
        seasonService.save(season);
        return "redirect:/seasons";
    }
    
    @Get("/{id}/edit")
    @View("seasons/form")
    public Map<String, Object> edit(Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("season", seasonService.findById(id).orElseThrow());
        model.put("leagues", leagueService.findAll());
        model.put("action", "/seasons/" + id);
        return model;
    }
    
    @Post("/{id}")
    public String update(Long id, @Body Season season) {
        seasonService.update(id, season);
        return "redirect:/seasons";
    }
    
    @Get("/{id}/delete")
    public String delete(Long id) {
        seasonService.deleteById(id);
        return "redirect:/seasons";
    }
}
