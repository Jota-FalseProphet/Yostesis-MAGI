package com.magi.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.magi.api.model.HorarioDocenteSesion;
import com.magi.api.service.HorarioDocenteSesionService;

@RestController
@RequestMapping("/api/horario-sesiones")
public class HorarioDocenteSesionController {

    private final HorarioDocenteSesionService service;

    public HorarioDocenteSesionController(HorarioDocenteSesionService service) {
        this.service = service;
    }

    // GET /api/horario-sesiones        -> todo
    // GET /api/horario-sesiones?dia=L  -> filtra por día
    @GetMapping
    public List<HorarioDocenteSesion> listar(@RequestParam(required = false) String dia) {
        return service.listar(dia);
    }
}
