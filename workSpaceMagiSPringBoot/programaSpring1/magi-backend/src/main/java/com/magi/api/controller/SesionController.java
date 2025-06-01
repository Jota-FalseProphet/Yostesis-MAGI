package com.magi.api.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.magi.api.dto.SesionDTO;
import com.magi.api.model.SessionHorario;
import com.magi.api.repository.SessionHorarioRepository;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {

    private final SessionHorarioRepository repo;

    public SesionController(SessionHorarioRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/sesiones?docenteId=3&fecha=2025-06-10
     * Devuelve los tramos que imparte el docente ese día.
     * Lógica:
     *   1) Convertir LocalDate → abreviatura de día (L, M, X, J, V, S, D).
     *   2) Si no hay filas, reintentar con nombre completo (“Lunes”, “Martes”, …).
     *   3) Mapear cada SessionHorario a SesionDTO, manejando posible grupo nulo.
     */
    @GetMapping
    public List<SesionDTO> sesionesDeDia(
            @RequestParam Integer docenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        // 1) Intento con la abreviatura de día (L, M, X, J, V, S, D)
        String claveDia = abreviaturaDia(fecha);
        List<SessionHorario> sesiones = repo.findByDocenteAndDia(docenteId, claveDia);

        // 2) Si no hay resultados, pruebo con nombre completo (“Lunes”, “Martes”…)
        if (sesiones.isEmpty()) {
            claveDia = nombreCompletoDia(fecha);  // “Lunes”, “Martes”, …
            sesiones = repo.findByDocenteAndDia(docenteId, claveDia);
        }

        // 3) Mapear a DTO (con null-safe para el nombre de grupo)
        return sesiones.stream()
                .map(sh -> {
                    String nombreGrupo = null;
                    if (sh.getGrupo() != null) {
                        nombreGrupo = sh.getGrupo().getNomGrupo();
                    }
                    return new SesionDTO(
                            sh.getIdSessio(),
                            sh.getHoraDesde().toString(),
                            sh.getHoraFins().toString(),
                            nombreGrupo
                    );
                })
                .collect(Collectors.toList());
    }

    /* ====================== MÉTODOS AUXILIARES ====================== */

    /** Devuelve “L”, “M”, “X”, “J”, “V”, “S” o “D” según el DayOfWeek */
    private String abreviaturaDia(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY    -> "L";
            case TUESDAY   -> "M";
            case WEDNESDAY -> "X";
            case THURSDAY  -> "J";
            case FRIDAY    -> "V";
            case SATURDAY  -> "S";
            case SUNDAY    -> "D";
        };
    }

    /** Devuelve “Lunes”, “Martes”, … (primera letra en mayúscula) */
    private String nombreCompletoDia(LocalDate fecha) {
        DayOfWeek dow = fecha.getDayOfWeek();
        String nombre = dow.getDisplayName(TextStyle.FULL, new Locale("es", "ES")); // “lunes”
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);       // “Lunes”
    }
}
