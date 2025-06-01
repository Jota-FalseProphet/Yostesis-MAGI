package com.magi.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.magi.api.dto.AusenciaIdDTO;
import com.magi.api.dto.CrearAusenciaDTO;
import com.magi.api.service.AusenciaService;

@RestController
@RequestMapping("/api/ausencias")
public class AusenciaController {

    private final AusenciaService service;

    public AusenciaController(AusenciaService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AusenciaIdDTO> crear(@RequestBody CrearAusenciaDTO dto) {
        int id = service.crearAusencia(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(new AusenciaIdDTO(id));
    }
}
