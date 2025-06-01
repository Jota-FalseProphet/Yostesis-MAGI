package com.magi.api.service;

import com.magi.api.dto.SesionDTO;
import com.magi.api.model.SessionHorario;
import com.magi.api.repository.SessionHorarioRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SesionService {

    private final SessionHorarioRepository repo;

    public SesionService(SessionHorarioRepository repo) {
        this.repo = repo;
    }

    /**
     * Devuelve las sesiones que imparte un docente en la fecha indicada.
     * 1. Convierte la fecha a clave de día que está guardada en la BD
     *    (primero abreviatura «L», «M», «X», …; si no hay resultados,
     *    prueba con el nombre completo «Lunes», «Martes», …).
     * 2. Mapea cada SessionHorario → SesionDTO.
     */
    public List<SesionDTO> obtenerSesiones(Integer docenteId, LocalDate fecha) {

        /* --- 1) Intento con la abreviatura (L-V, S, D) ------------------- */
        String claveDia = abreviaturaDia(fecha);
        List<SessionHorario> sesiones = repo.findByDocenteAndDia(docenteId, claveDia);

        /* --- 2) Si no hay resultados, pruebo con el nombre completo ------- */
        if (sesiones.isEmpty()) {
            claveDia = nombreCompletoDia(fecha);          // «Lunes», «Martes», …
            sesiones = repo.findByDocenteAndDia(docenteId, claveDia);
        }

        /* --- 3) Mapeo a DTO ------------------------------------------------ */
        return sesiones.stream()
                .map(sh -> new SesionDTO(
                        sh.getIdSessio(),
                        sh.getHoraDesde().toString(),
                        sh.getHoraFins().toString(),
                        sh.getGrupo().getNomGrupo()))
                .collect(Collectors.toList());
    }

    /* ==================================================================== */
    /* Utilidades                                                           */
    /* ==================================================================== */

    /** Devuelve «L», «M», «X», «J», «V», «S» o «D» según el día de la fecha */
    private String abreviaturaDia(LocalDate f) {
        return switch (f.getDayOfWeek()) {
            case MONDAY    -> "L";
            case TUESDAY   -> "M";
            case WEDNESDAY -> "X";   // o «Mi» según tu BD
            case THURSDAY  -> "J";
            case FRIDAY    -> "V";
            case SATURDAY  -> "S";
            case SUNDAY    -> "D";
        };
    }

    /** Devuelve «Lunes», «Martes», … con mayúscula inicial */
    private String nombreCompletoDia(LocalDate f) {
        DayOfWeek dow = f.getDayOfWeek();
        String n = dow.getDisplayName(TextStyle.FULL, new Locale("es", "ES")); // «lunes»
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);            // «Lunes»
    }
}
