package com.magi.api.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.magi.api.model.HorarioDocenteSesion;
import com.magi.api.repository.HorarioDocenteSesionRepository;
//servicio para marcar los hrarios del profe, creo que lo usaré para meter las franjas que no vaya a asistir
@Service
public class HorarioDocenteSesionService {

    private final HorarioDocenteSesionRepository repo;

    public HorarioDocenteSesionService(HorarioDocenteSesionRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<HorarioDocenteSesion> listar(String dia) {
        if (dia == null || dia.isBlank()) return repo.findAll();
        return repo.findByDiaSemana(dia.trim());
    }
}
