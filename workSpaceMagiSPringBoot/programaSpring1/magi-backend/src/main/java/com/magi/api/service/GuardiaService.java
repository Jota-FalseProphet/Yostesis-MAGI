package com.magi.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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

@Service
public class GuardiaService {

    private final GuardiaRepository guardiaRepo;
    private final DocentRepository docentRepo;
    private final AusenciaSessioRepository ausenciaRepo;
    private final AusenciasProperties props;
    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    public GuardiaService(
        GuardiaRepository guardiaRepo,
        DocentRepository docentRepo,
        AusenciaSessioRepository ausenciaRepo,
        AusenciasProperties props
    ) {
        this.guardiaRepo  = guardiaRepo;
        this.docentRepo   = docentRepo;
        this.ausenciaRepo = ausenciaRepo;
        this.props        = props;
    }

    @Transactional(readOnly = true)
    public List<SessionGuardiaDTO> listarAusenciasVigentes(LocalDate fecha) {
        LocalTime ahora = LocalTime.now(MADRID);
        return ausenciaRepo.findGuardiasVigentes(
            fecha,
            ahora,
            props.getGraciaMin()
        );
    }

    @Transactional(readOnly = true)
    public List<SessionGuardiaDTO> listarAusenciasDelDia(LocalDate fecha) {
        return ausenciaRepo.findGuardiasDelDia(fecha);
    }

    @Transactional
    public void asignarGuardia(String dniAsignat, Long idSessioLong) {
        Integer idSessio = idSessioLong.intValue();
        LocalDate hoy    = LocalDate.now(MADRID);

        // 1. Obtiene el docente que va a cubrir
        Docent asignat = docentRepo.findByDni(dniAsignat.trim())
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor asignado no encontrado")
            );

        // 2. Verifica que haya al menos una ausencia
        Docent absent = ausenciaRepo.findFirstDocentAbsentBySessionAndFecha(idSessio, hoy)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay ausencia para esa sesión")
            );

        // 3. Inserta o actualiza en un solo paso (INSERT … ON CONFLICT)
        //   Convertimos los Integer a Long para que encajen con el método del repo:
        guardiaRepo.cubrir(
            asignat.getIdDocent().longValue(),   // <-- aquí
            absent.getIdDocent().longValue(),    // <-- y aquí
            idSessio,
            hoy
        );
    }

    @Transactional(readOnly = true)
    public List<Guardia> historicoGuardias() {
        Iterable<Guardia> it = guardiaRepo.findAll();
        List<Guardia> lista = new ArrayList<>();
        it.forEach(lista::add);
        return lista;
    }
}
