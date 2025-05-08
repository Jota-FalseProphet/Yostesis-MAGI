package com.magi.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.magi.api.service.FichajeService;

@RestController
@RequestMapping("/api/fichaje")
public class FichajeController {

    private final FichajeService service;

    public FichajeController(FichajeService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ResponseEntity<String> start(Authentication auth) {
        service.iniciar(auth.getName());
        return ResponseEntity.ok("INICIADO");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop(Authentication auth) {
        service.finalizar(auth.getName());
        return ResponseEntity.ok("FINALIZADO");
    }
}
