package com.scheduleengine.controller;

import com.scheduleengine.service.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {
    
    private final LeagueService leagueService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    private final GameService gameService;
    
    public HomeController(LeagueService leagueService, TeamService teamService, 
                         FieldService fieldService, SeasonService seasonService,
                         GameService gameService) {
        this.leagueService = leagueService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
        this.gameService = gameService;
    }
    
    @Get("/")
    @View("index")
    public Map<String, Object> index() {
        Map<String, Object> model = new HashMap<>();
        model.put("leagueCount", leagueService.findAll().size());
        model.put("teamCount", teamService.findAll().size());
        model.put("fieldCount", fieldService.findAll().size());
        model.put("seasonCount", seasonService.findAll().size());
        model.put("gameCount", gameService.findAll().size());
        return model;
    }
}
