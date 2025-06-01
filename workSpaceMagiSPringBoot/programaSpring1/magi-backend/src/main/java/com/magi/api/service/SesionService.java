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

    //devuelve las sesiones que imparte un docente en la fecha indicada
     //kluego convierto la fecha a clave de dia que está guardada en la BD
    public List<SesionDTO> obtenerSesiones(Integer docenteId, LocalDate fecha) {

        //aqi intento con la abreviatura
        String claveDia = abreviaturaDia(fecha);
        List<SessionHorario> sesiones = repo.findByDocenteAndDia(docenteId, claveDia);

        //pero si no hay resultados pruebo con el nombre completo
        if (sesiones.isEmpty()) {
            claveDia = nombreCompletoDia(fecha);         
            sesiones = repo.findByDocenteAndDia(docenteId, claveDia);
        }

        //mapeo del dto
        return sesiones.stream()
                .map(sh -> new SesionDTO(
                        sh.getIdSessio(),
                        sh.getHoraDesde().toString(),
                        sh.getHoraFins().toString(),
                        sh.getGrupo().getNomGrupo()))
                .collect(Collectors.toList());
    }

    
    private String abreviaturaDia(LocalDate f) {
        return switch (f.getDayOfWeek()) {
            case MONDAY    -> "L";
            case TUESDAY   -> "M";
            case WEDNESDAY -> "X"; //en la base de datos uso X
            case THURSDAY  -> "J";
            case FRIDAY    -> "V";
            case SATURDAY  -> "S";
            case SUNDAY    -> "D";
        };
    }

 //devuelve dias de la semana con mayuscula inicial
    private String nombreCompletoDia(LocalDate f) {
        DayOfWeek dow = f.getDayOfWeek();
        String n = dow.getDisplayName(TextStyle.FULL, new Locale("es", "ES")); // lunes
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);            // Lunes
    }
}
