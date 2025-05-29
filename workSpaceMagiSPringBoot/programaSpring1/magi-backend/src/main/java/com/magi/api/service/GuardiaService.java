package com.magi.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.config.AusenciasProperties;
import com.magi.api.dto.GuardiaHistoricoDTO;
import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.Docent;
import com.magi.api.repository.AusenciaSessioRepository;
import com.magi.api.repository.DocentRepository;
import com.magi.api.repository.GuardiaRepository;

@Service
public class GuardiaService {

    private final GuardiaRepository guardiaRepo;
    private final DocentRepository    docentRepo;
    private final AusenciaSessioRepository ausenciaRepo;
    private final AusenciasProperties props;
    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    public GuardiaService(GuardiaRepository guardiaRepo,
                          DocentRepository docentRepo,
                          AusenciaSessioRepository ausenciaRepo,
                          AusenciasProperties props) {
        this.guardiaRepo  = guardiaRepo;
        this.docentRepo   = docentRepo;
        this.ausenciaRepo = ausenciaRepo;
        this.props        = props;
    }

    @Transactional(readOnly=true)
    public List<SessionGuardiaDTO> listarAusenciasVigentes(LocalDate fecha) {
        LocalTime ahora = LocalTime.now(MADRID);
        return ausenciaRepo.findGuardiasVigentes(fecha, ahora, props.getGraciaMin());
    }

    @Transactional(readOnly=true)
    public List<SessionGuardiaDTO> listarAusenciasDelDia(LocalDate fecha) {
        return ausenciaRepo.findGuardiasDelDia(fecha);
    }

    @Transactional
    public void asignarGuardia(String dniAsignat, Long idSessioLong) {
        Integer idSessio = idSessioLong.intValue();
        LocalDate hoy = LocalDate.now(MADRID);

        Docent asignat = docentRepo.findByDni(dniAsignat.trim())
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor asignado no encontrado")
            );

        Docent absent = ausenciaRepo.findFirstDocentAbsentBySessionAndFecha(idSessio, hoy)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay ausencia para esa sesión")
            );

        try {
            guardiaRepo.cubrir(
                asignat.getIdDocent().longValue(),
                absent.getIdDocent().longValue(),
                idSessio,
                hoy
            );
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            if (root instanceof PSQLException && "22023".equals(((PSQLException) root).getSQLState())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, root.getMessage());
            }
            throw ex;
        }
    }

    @Transactional(readOnly=true)
    public List<GuardiaHistoricoDTO> historicoGuardiasPorDni(String dni) {
        return guardiaRepo.findByDocentAssignatDni(dni).stream()
            .map(g -> {
                var ses = g.getSession();
                String grupo = (ses!=null && ses.getGrupo()!=null)
                              ? ses.getGrupo().getNomGrupo() : "—";
                String aula  = (ses!=null && ses.getAula()!=null)
                              ? ses.getAula().getNombre()   : "—";
                return new GuardiaHistoricoDTO(
                    g.getId(),
                    g.getDocentAssignat().getDni(),
                    g.getDocentAbsent().getDni(),
                    grupo,
                    aula,
                    g.getFechaGuardia()
                );
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public Map<String,List<GuardiaHistoricoDTO>> historicoAgrupadoPorDocente() {
        return guardiaRepo.findAll().stream()
            .map(g -> {
                var ses = g.getSession();
                String grupo = (ses!=null && ses.getGrupo()!=null)
                              ? ses.getGrupo().getNomGrupo() : "—";
                String aula  = (ses!=null && ses.getAula()!=null)
                              ? ses.getAula().getNombre()   : "—";
                return new GuardiaHistoricoDTO(
                    g.getId(),
                    g.getDocentAssignat().getDni(),
                    g.getDocentAbsent().getDni(),
                    grupo,
                    aula,
                    g.getFechaGuardia()
                );
            })
            .collect(Collectors.groupingBy(GuardiaHistoricoDTO::getDniAsignat));
    }
}
