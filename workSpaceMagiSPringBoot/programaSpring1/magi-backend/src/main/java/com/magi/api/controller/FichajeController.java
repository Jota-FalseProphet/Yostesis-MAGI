package com.magi.api.controller;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.model.Fichaje;
import com.magi.api.service.FichajeService;

@RestController
@RequestMapping("/api/fichaje")
public class FichajeController {

    private final FichajeService service;

    public FichajeController(FichajeService service) {
        this.service = service;
    }

   
    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestParam("dni") String dni) {
        try {
            Fichaje ficha = service.iniciar(dni.trim());
            return ResponseEntity.ok(ficha);
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Collections.singletonMap("error", ex.getReason()));
        }
    }

    
    @PostMapping("/end")
    public ResponseEntity<?> end(@RequestParam("dni") String dni) {
        try {
            Fichaje ficha = service.finalizar(dni.trim());
            return ResponseEntity.ok(ficha);
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Collections.singletonMap("error", ex.getReason()));
        }
    }
}
