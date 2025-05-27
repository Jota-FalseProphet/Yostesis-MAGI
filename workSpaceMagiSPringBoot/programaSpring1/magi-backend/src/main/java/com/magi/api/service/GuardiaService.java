package com.magi.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;               // ← import añadido
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.config.AusenciasProperties;
import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.Docent;
import com.magi.api.model.Guardia;
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
        // Usamos CET (Europe/Madrid) para filtrar correctamente las sesiones de tarde
        LocalTime ahora = LocalTime.now(ZoneId.of("Europe/Madrid"));
        return ausenciaSessioRepo.findGuardiasVigentes(
            fecha,
            ahora,
            props.getGraciaMin()
        );
    }

    @Transactional(readOnly = true)
    public List<SessionGuardiaDTO> listarAusenciasDelDia(LocalDate fecha) {
        return ausenciaSessioRepo.findGuardiasDelDia(fecha);
    }

    @Transactional
    public void asignarGuardia(String dniAsignat, Long idSessioLong) {
        Integer idSessio = idSessioLong.intValue();
        LocalDate hoy = LocalDate.now();

        // 1. Obtiene el docente que va a cubrir
        Docent asignat = docentRepo.findByDni(dniAsignat.trim())
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor asignado no encontrado")
            );

        // 2. Verifica que existe una ausencia previa para esa sesión y fecha
        ausenciaSessioRepo.findDocentAbsentBySessionAndFecha(idSessio, hoy)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay ausencia para esa sesión")
            );

        // 3. Ejecuta el UPDATE sobre la guardia ya generada
        int updated = guardiaRepo.asignarGuardia(
            asignat,
            idSessio,
            hoy
        );

        // 4. Si no actualiza ninguna fila, significa que no había guardia pendiente
        if (updated == 0) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe guardia pendiente para la sesión " + idSessio + " en " + hoy
            );
        }
    }

    @Transactional(readOnly = true)
    public List<Guardia> historicoGuardias() {
        return guardiaRepo.findAll();
    }
}
