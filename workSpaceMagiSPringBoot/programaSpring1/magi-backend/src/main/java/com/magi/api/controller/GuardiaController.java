// src/main/java/com/magi/api/controller/GuardiaController.java
package com.magi.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.magi.api.model.Guardia;
import com.magi.api.model.SessionHorario;
import com.magi.api.service.GuardiaService;

@RestController
@RequestMapping("/api/guardias")
public class GuardiaController {

    private final GuardiaService service;

    public GuardiaController(GuardiaService service) {
        this.service = service;
    }

    /** Listar las sesiones sin cubrir hoy */
    @GetMapping("/ausencias/vigentes")
    public List<SessionHorario> ausenciasVigentes() {
        return service.listarAusenciasVigentes(LocalDate.now());
    }

    /** Asignar guardia: /api/guardias?dniAsignat=1234A&idSessio=5 */
    @PostMapping
    public Guardia crear(
        @RequestParam String dniAsignat,
        @RequestParam Long idSessio
    ) {
        return service.asignarGuardia(dniAsignat, idSessio);
    }

    /** Histórico de todas las guardias */
    @GetMapping("/historico")
    public List<Guardia> historico() {
        return service.historicoGuardias();
    }
}
