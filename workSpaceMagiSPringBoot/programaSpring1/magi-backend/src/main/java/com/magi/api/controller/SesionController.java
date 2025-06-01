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

    @GetMapping
    public List<SesionDTO> sesionesDeDia(
            @RequestParam Integer docenteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

     
        String claveDia = abreviaturaDia(fecha);
        List<SessionHorario> sesiones = repo.findByDocenteAndDia(docenteId, claveDia);

    
        if (sesiones.isEmpty()) {
            claveDia = nombreCompletoDia(fecha); 
            sesiones = repo.findByDocenteAndDia(docenteId, claveDia);
        }

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

    //devuelve la letra segund DayOfWeek
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

    private String nombreCompletoDia(LocalDate fecha) {
        DayOfWeek dow = fecha.getDayOfWeek();
        String nombre = dow.getDisplayName(TextStyle.FULL, new Locale("es", "ES")); 
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);   
    }
}
