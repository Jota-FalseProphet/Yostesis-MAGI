package com.magi.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

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

    /**
     * Listar las sesiones sin cubrir hoy (DTO)
     */
    @GetMapping("/ausencias/vigentes")
    public List<SessionGuardiaDTO> ausenciasVigentes() {
        return service.listarAusenciasVigentes(LocalDate.now());
    }

    /**
     * Asignar guardia:
     * POST /api/guardias/asignar?dniAsignat=1234A&idSessio=5
     */
    @PostMapping("/asignar")
    public ResponseEntity<?> crear(
            @RequestParam("dniAsignat") String dniAsignat,
            @RequestParam("idSessio") Long idSessio
    ) {
        try {
            Guardia guardia = service.asignarGuardia(dniAsignat.trim(), idSessio);
            return ResponseEntity.ok(guardia);
        } catch (ResponseStatusException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Collections.singletonMap("error", ex.getReason()));
        }
    }

    /**
     * Histórico de todas las guardias
     */
    @GetMapping("/historico")
    public List<Guardia> historico() {
        return service.historicoGuardias();
    }
}
