package com.magi.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.Guardia;
import com.magi.api.service.GuardiaService;

@RestController
@RequestMapping("/api/guardias")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GuardiaController {

    private final GuardiaService service;

    public GuardiaController(GuardiaService service) {
        this.service = service;
    }

    @GetMapping("/ausencias/vigentes")
    public List<SessionGuardiaDTO> ausenciasVigentes() {
        return service.listarAusenciasVigentes(LocalDate.now());
    }

    @GetMapping("/ausencias/dia")
    public List<SessionGuardiaDTO> ausenciasDelDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = (fecha != null ? fecha : LocalDate.now());
        return service.listarAusenciasDelDia(dia);
    }

    /**
     * Asigna un docente a una guardia existente.
     * Ejemplo:
     *   POST https://magi.it.com/api/guardias/asignar
     *         ?dniAsignat=070374673Z
     *         &idSessio=598
     */
    @PostMapping("/asignar")
    public ResponseEntity<Void> crear(
            @RequestParam("dniAsignat") String dniAsignat,
            @RequestParam("idSessio")  Long   idSessio) {
        try {
            service.asignarGuardia(dniAsignat.trim(), idSessio);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    /**
     * Cubre una guardia (misma lógica que asignar).
     * Ejemplo:
     *   POST https://magi.it.com/api/guardias/cubrir
     *         ?dniAsignat=070374673Z
     *         &idSessio=598
     */
    @PostMapping("/cubrir")
    public ResponseEntity<Void> cubrir(
            @RequestParam("dniAsignat") String dniAsignat,
            @RequestParam("idSessio")  Long   idSessio) {
        try {
            service.asignarGuardia(dniAsignat.trim(), idSessio);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }
    
    @GetMapping("/historico")
    public List<Guardia> historico() {
        return service.historicoGuardias();
    }
}
