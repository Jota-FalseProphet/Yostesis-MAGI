package com.magi.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.MediaType;
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

    @PostMapping(path = "/asignar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> crear(@RequestBody AsignacionRequest req) {
        try {
            service.asignarGuardia(req.getDniAsignat().trim(), req.getIdSessio());
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @PostMapping(path = "/cubrir", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cubrir(@RequestBody AsignacionRequest req) {
        try {
            service.asignarGuardia(req.getDniAsignat().trim(), req.getIdSessio());
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @GetMapping("/historico")
    public List<Guardia> historico() {
        return service.historicoGuardias();
    }

    public static class AsignacionRequest {
        private String dniAsignat;
        private Long idSessio;

        public String getDniAsignat() {
            return dniAsignat;
        }

        public void setDniAsignat(String dniAsignat) {
            this.dniAsignat = dniAsignat;
        }

        public Long getIdSessio() {
            return idSessio;
        }

        public void setIdSessio(Long idSessio) {
            this.idSessio = idSessio;
        }
    }
}
