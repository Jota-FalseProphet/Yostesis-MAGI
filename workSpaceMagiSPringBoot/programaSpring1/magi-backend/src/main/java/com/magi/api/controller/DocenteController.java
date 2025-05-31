package com.magi.api.controller;

import com.magi.api.dto.DocenteDTO;
import com.magi.api.service.DocenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    /**
     * GET /api/docentes
     * Devuelve la lista de todos los docentes, en el formato que espera la app Android:
     * [
     *   { "id": 1, "nombre": "María Pérez Gómez" },
     *   { "id": 2, "nombre": "Juan Martínez López" },
     *   ...
     * ]
     */
    @GetMapping
    public ResponseEntity<List<DocenteDTO>> listarDocentes() {
        List<DocenteDTO> lista = docenteService.listarTodos();
        return ResponseEntity.ok(lista);
    }
}
