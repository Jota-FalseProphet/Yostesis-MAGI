package com.magi.api.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.AusenciaSessio;
import com.magi.api.model.AusenciaSessioId;
import com.magi.api.model.Docent;
import com.magi.api.model.SessionHorario;

/**
 * Repositorio para ausencias vinculadas a sesiones lectivas.
 * <p>
 * Incluye la consulta nativa que filtra por fecha, hora actual y margen
 * de cortesía configurable.  Se une por <code>id_docent</code> (no por DNI)
 * que es la FK real en la tabla <code>ausencies</code>.
 */
public interface AusenciaSessioRepository
    extends JpaRepository<AusenciaSessio, AusenciaSessioId> {

    /**
     * Devuelve las guardias que están vigentes "ahora" (entre hora_desde + margen y hora_fins)
     * y que solo requieren un docente de guardia.
     *
     * @param fecha     Día de consulta (normalmente hoy)
     * @param ahora     Hora actual en zona Europe/Madrid (se inyecta desde el servicio)
     * @param graciaMin Margen de cortesía en minutos (configurable)
     */
    @Query(value = """
SELECT  sh.id_sessio,
        sh.dia_setmana,
        sh.hora_desde,
        sh.hora_fins,
        sh.plantilla,
        aul.nombre                                    AS aula,
        d.dni,
        d.nom
  FROM ausencies_sessio    ass
  JOIN sessions_horari     sh  ON sh.id_sessio = ass.id_sessio
  JOIN ausencies           au  ON au.id_ausencia = ass.id_ausencia
  JOIN docents             d   ON d.id_docent   = au.id_docent
  JOIN aules               aul ON aul.id_aula   = sh.id_aula
 WHERE au.fecha_ausencia = :fecha
   AND au.is_full_day   = FALSE
   AND :ahora BETWEEN (sh.hora_desde + make_interval(mins => :graciaMin))
                   AND  sh.hora_fins
   AND ( SELECT COUNT(*)
           FROM docents_sessio ds
          WHERE ds.id_sessio = sh.id_sessio) = 1
""", nativeQuery = true)
    List<SessionGuardiaDTO> findGuardiasVigentes(
            @Param("fecha")     LocalDate fecha,
            @Param("ahora")     LocalTime ahora,
            @Param("graciaMin") int graciaMin);

    // -- Métodos de soporte usados por el servicio ---------------------------

    @Query("""
        SELECT s.session
          FROM AusenciaSessio s
         WHERE s.ausencia.fechaAusencia = :fecha
    """)
    List<SessionHorario> findSessionsByFecha(@Param("fecha") LocalDate fecha);

    @Query("""
        SELECT s.ausencia.docent
          FROM AusenciaSessio s
         WHERE s.session.idSessio     = :idSessio
           AND s.ausencia.fechaAusencia = :fecha
    """)
    Optional<Docent> findDocentAbsentBySessionAndFecha(
        @Param("idSessio") Integer idSessio,
        @Param("fecha")    LocalDate fecha
    );
}
