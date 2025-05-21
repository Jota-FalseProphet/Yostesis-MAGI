// src/main/java/com/magi/api/service/GuardiaService.java
package com.magi.api.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.Docent;
import com.magi.api.model.Guardia;
import com.magi.api.model.SessionHorario;
import com.magi.api.repository.AusenciaSessioRepository;
import com.magi.api.repository.DocentRepository;
import com.magi.api.repository.GuardiaRepository;
import com.magi.api.repository.SessionHorarioRepository;

@Service
public class GuardiaService {

    private final GuardiaRepository guardiaRepo;
    private final DocentRepository docentRepo;
    private final SessionHorarioRepository sessionRepo;
    private final AusenciaSessioRepository ausenciaSessioRepo;

    public GuardiaService(
        GuardiaRepository guardiaRepo,
        DocentRepository docentRepo,
        SessionHorarioRepository sessionRepo,
        AusenciaSessioRepository ausenciaSessioRepo
    ) {
        this.guardiaRepo        = guardiaRepo;
        this.docentRepo         = docentRepo;
        this.sessionRepo        = sessionRepo;
        this.ausenciaSessioRepo = ausenciaSessioRepo;
    }

    /** 1) Devuelve las sesiones sin cubrir hoy, con DTO */
    @Transactional(readOnly = true)
    public List<SessionGuardiaDTO> listarAusenciasVigentes(LocalDate fecha) {
        return ausenciaSessioRepo.findGuardiasVigentes(fecha);
    }

    /** 2) Crea una guardia a partir de DNI del asignado y id de sesión */
    @Transactional
    public Guardia asignarGuardia(String dniAsignat, Long idSessioLong) {
        Integer idSessio = idSessioLong.intValue();
        LocalDate hoy = LocalDate.now();

        // 2.1) Buscar docente que cubrirá la guardia
        Docent asignat = docentRepo.findByDni(dniAsignat.trim())
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor asignado no encontrado")
            );

        // 2.2) Obtener la sesión lectiva
        SessionHorario session = sessionRepo.findById(idSessio)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sesión no encontrada")
            );

        // 2.3) Encontrar el docente ausente para esa sesión y fecha
        Docent absent = ausenciaSessioRepo
            .findDocentAbsentBySessionAndFecha(idSessio, hoy)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay ausencia para esa sesión")
            );

        // 2.4) Chequear duplicado antes de persistir
        if (guardiaRepo.existsByDocentAbsentAndSessionAndFechaGuardia(absent, session, hoy)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe una guardia para ese profesor, sesión y fecha"
            );
        }

        // 2.5) Crear y guardar la guardia
        Guardia g = new Guardia(asignat, absent, session);
        g.setFechaGuardia(hoy);
        return guardiaRepo.save(g);
    }

    /** 3) Histórico completo de todas las guardias realizadas */
    @Transactional(readOnly = true)
    public List<Guardia> historicoGuardias() {
        return guardiaRepo.findAll();
    }
}
