package com.scheduleengine.controller;

import com.scheduleengine.domain.Field;
import com.scheduleengine.service.FieldService;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/fields")
public class FieldController {
    
    private final FieldService fieldService;
    
    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }
    
    @Get
    @View("fields/list")
    public Map<String, Object> list() {
        Map<String, Object> model = new HashMap<>();
        model.put("fields", fieldService.findAll());
        return model;
    }
    
    @Get("/new")
    @View("fields/form")
    public Map<String, Object> newField() {
        Map<String, Object> model = new HashMap<>();
        model.put("field", new Field());
        model.put("action", "/fields");
        return model;
    }
    
    @Post
    public String create(@Body Field field) {
        fieldService.save(field);
        return "redirect:/fields";
    }
    
    @Get("/{id}/edit")
    @View("fields/form")
    public Map<String, Object> edit(Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("field", fieldService.findById(id).orElseThrow());
        model.put("action", "/fields/" + id);
        return model;
    }
    
    @Post("/{id}")
    public String update(Long id, @Body Field field) {
        fieldService.update(id, field);
        return "redirect:/fields";
    }
    
    @Get("/{id}/delete")
    public String delete(Long id) {
        fieldService.deleteById(id);
        return "redirect:/fields";
    }
}
