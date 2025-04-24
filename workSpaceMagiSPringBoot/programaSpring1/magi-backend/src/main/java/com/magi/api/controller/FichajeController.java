package com.magi.api.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.magi.api.model.Fichaje;
import com.magi.api.repository.FichajeRepository;

@RestController
@RequestMapping("/api/fichaje")
public class FichajeController {

    private final FichajeRepository repo;

    public FichajeController(FichajeRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/in")
    public ResponseEntity<Fichaje> ficharEntrada(@RequestParam String dni) {

        Fichaje fichaje = new Fichaje(
            dni,
            LocalDateTime.now(ZoneId.of("Europe/Madrid")),
            Fichaje.Tipo.IN
        );
        return ResponseEntity.ok(repo.save(fichaje));
    }

    @PostMapping("/out")
    public ResponseEntity<Fichaje> ficharSalida(@RequestParam String dni) {

        Fichaje fichaje = new Fichaje(
            dni,
            LocalDateTime.now(ZoneId.of("Europe/Madrid")),
            Fichaje.Tipo.OUT
        );
        return ResponseEntity.ok(repo.save(fichaje));
    }
}
