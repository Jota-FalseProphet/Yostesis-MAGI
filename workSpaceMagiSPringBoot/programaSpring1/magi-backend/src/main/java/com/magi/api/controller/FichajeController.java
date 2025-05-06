package com.magi.api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.magi.api.model.Fichaje;
import com.magi.api.service.FichajeService;

@RestController
@RequestMapping("/api/fichaje")
public class FichajeController {

    private final FichajeService service;

    public FichajeController(FichajeService service) { this.service = service; }

    @PostMapping("/start")
    public Fichaje start(Authentication auth) {
        return service.iniciar(dniDe(auth));
    }

    @PostMapping("/end")
    public Fichaje end(Authentication auth) {
        return service.finalizar(dniDe(auth));
    }

    /** Extrae el DNI que Spring Security coloca como ‘username’ */
    private String dniDe(Authentication auth) {
        return ((UserDetails) auth.getPrincipal()).getUsername();
    }
}
