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

    /** 
     * Inicia fichaje.
     * Ejemplo de llamada:
     *   POST https://159.69.215.108:8443/api/fichaje/start?dni=12345678A
     */
    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestParam("dni") String dni) {
        try {
            Fichaje ficha = service.iniciar(dni.trim());
            return ResponseEntity.ok(ficha);
        } catch (ResponseStatusException ex) {
            // construye un body { "error": "mensaje" } con el status que vino del servicio
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Collections.singletonMap("error", ex.getReason()));
        }
    }

    /** 
     * Finaliza fichaje.
     * Ejemplo de llamada:
     *   POST https://159.69.215.108:8443/api/fichaje/end?dni=12345678A
     */
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
