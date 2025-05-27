package com.magi.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.config.AusenciasProperties;
import com.magi.api.dto.SessionGuardiaDTO;
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
    private final AusenciasProperties props;

    public GuardiaService(
        GuardiaRepository guardiaRepo,
        DocentRepository docentRepo,
        SessionHorarioRepository sessionRepo,
        AusenciaSessioRepository ausenciaSessioRepo,
        AusenciasProperties props
    ) {
        this.guardiaRepo        = guardiaRepo;
        this.docentRepo         = docentRepo;
        this.sessionRepo        = sessionRepo;
        this.ausenciaSessioRepo = ausenciaSessioRepo;
        this.props              = props;
    }

    @Transactional(readOnly = true)
    public List<SessionGuardiaDTO> listarAusenciasVigentes(LocalDate fecha) {
        return ausenciaSessioRepo.findGuardiasVigentes(
            fecha,
            LocalTime.now(),
            props.getGraciaMin()
        );
    }

    @Transactional
    public void asignarGuardia(String dniAsignat, Long idSessioLong) {
        Integer idSessio = idSessioLong.intValue();
        LocalDate hoy = LocalDate.now();

        var asignat = docentRepo.findByDni(dniAsignat.trim())
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor asignado no encontrado")
            );

        var session = sessionRepo.findById(idSessio)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sesión no encontrada")
            );

        var absent = ausenciaSessioRepo.findDocentAbsentBySessionAndFecha(idSessio, hoy)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay ausencia para esa sesión")
            );

        guardiaRepo.cubrir(session.getIdSessio(), hoy, asignat.getIdDocent());
    }

    @Transactional(readOnly = true)
    public List<com.magi.api.model.Guardia> historicoGuardias() {
        return guardiaRepo.findAll();
    }
}
