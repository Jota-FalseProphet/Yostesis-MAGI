package com.magi.api.controller;

import org.springframework.web.bind.annotation.*;

import com.magi.api.model.Fichaje;
import com.magi.api.service.FichajeService;

@RestController
@RequestMapping("/api/fichaje")
public class FichajeController {

    private final FichajeService service;

    public FichajeController(FichajeService service) {
        this.service = service;
    }

    /** 
     * Inicia fichaje.
     * Ejemplo de llamada:
     *   POST https://159.69.215.108:8443/api/fichaje/start?dni=12345678A
     */
    @PostMapping("/start")
    public Fichaje start(@RequestParam("dni") String dni) {
        return service.iniciar(dni.trim());
    }

    /** 
     * Finaliza fichaje.
     * Ejemplo de llamada:
     *   POST https://159.69.215.108:8443/api/fichaje/end?dni=12345678A
     */
    @PostMapping("/end")
    public Fichaje end(@RequestParam("dni") String dni) {
        return service.finalizar(dni.trim());
    }
}
