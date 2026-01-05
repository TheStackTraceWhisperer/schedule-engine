package com.scheduleengine.controller;

import com.scheduleengine.domain.Game;
import com.scheduleengine.service.*;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/games")
public class GameController {
    
    private final GameService gameService;
    private final TeamService teamService;
    private final FieldService fieldService;
    private final SeasonService seasonService;
    
    public GameController(GameService gameService, TeamService teamService,
                         FieldService fieldService, SeasonService seasonService) {
        this.gameService = gameService;
        this.teamService = teamService;
        this.fieldService = fieldService;
        this.seasonService = seasonService;
    }
    
    @Get
    @View("games/list")
    public Map<String, Object> list() {
        Map<String, Object> model = new HashMap<>();
        model.put("games", gameService.findAll());
        return model;
    }
    
    @Get("/new")
    @View("games/form")
    public Map<String, Object> newGame() {
        Map<String, Object> model = new HashMap<>();
        model.put("game", new Game());
        model.put("teams", teamService.findAll());
        model.put("fields", fieldService.findAll());
        model.put("seasons", seasonService.findAll());
        model.put("action", "/games");
        return model;
    }
    
    @Post
    public String create(@Body Game game) {
        gameService.save(game);
        return "redirect:/games";
    }
    
    @Get("/{id}/edit")
    @View("games/form")
    public Map<String, Object> edit(Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("game", gameService.findById(id).orElseThrow());
        model.put("teams", teamService.findAll());
        model.put("fields", fieldService.findAll());
        model.put("seasons", seasonService.findAll());
        model.put("action", "/games/" + id);
        return model;
    }
    
    @Post("/{id}")
    public String update(Long id, @Body Game game) {
        gameService.update(id, game);
        return "redirect:/games";
    }
    
    @Get("/{id}/delete")
    public String delete(Long id) {
        gameService.deleteById(id);
        return "redirect:/games";
    }
}
