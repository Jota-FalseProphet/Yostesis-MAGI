package com.magi.api.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.*;

public interface AusenciaSessioRepository
        extends JpaRepository<AusenciaSessio, AusenciaSessioId> {

    //Todas las sesiones de ausencias ya empezadas inicio y 5m de gracia devolviendo el nombre del grupo
    @Query(value = """
      SELECT sh.id_sessio         AS idSessio,
             sh.dia_setmana       AS diaSetmana,
             sh.hora_desde        AS horaDesde,
             sh.hora_fins         AS horaHasta,
             g.nombre 			  AS grupo,

             aul.nombre           AS aula,
             d.document           AS absenteDni,
             d.nom                AS absenteNombre,
             (gu.docent_assignat IS NOT NULL) AS cubierta,
             dc.nom               AS profesorGuardia
        FROM ausencies_sessio ass
        JOIN sessions_horari  sh  ON sh.id_sessio    = ass.id_sessio
        JOIN ausencies        au  ON au.id_ausencia  = ass.id_ausencia
        JOIN docent           d   ON d.id_docent     = au.id_docent
        JOIN aula             aul ON aul.id_aula      = sh.id_aula
   LEFT JOIN grupo       g   ON g.id_grupo       = sh.id_grupo
   LEFT JOIN guardies    gu  ON gu.id_sessio      = sh.id_sessio
                        AND gu.fecha_guardia  = :fecha
   LEFT JOIN docent      dc  ON dc.id_docent     = gu.docent_assignat
       WHERE au.fecha_ausencia  = :fecha
         AND au.is_full_day     = FALSE
         AND (sh.hora_desde + make_interval(mins => :graciaMin)) <= :ahora
       ORDER BY sh.hora_desde DESC
    """, nativeQuery = true)
    List<SessionGuardiaDTO> findGuardiasVigentes(
        @Param("fecha")     LocalDate fecha,
        @Param("ahora")     LocalTime ahora,
        @Param("graciaMin") int       graciaMin
    );

    //todas las ausencias del dia ()mi dia va de 6h a 23h por si acaso dar margen
    @Query(value = """
      SELECT sh.id_sessio         AS idSessio,
             sh.dia_setmana       AS diaSetmana,
             sh.hora_desde        AS horaDesde,
             sh.hora_fins         AS horaHasta,
             g.nombre 			  AS grupo,
             aul.nombre           AS aula,
             d.document           AS absenteDni,
             d.nom                AS absenteNombre,
             (gu.docent_assignat IS NOT NULL) AS cubierta,
             dc.nom               AS profesorGuardia
        FROM ausencies_sessio ass
        JOIN sessions_horari  sh  ON sh.id_sessio    = ass.id_sessio
        JOIN ausencies        au  ON au.id_ausencia  = ass.id_ausencia
        JOIN docent           d   ON d.id_docent     = au.id_docent
        JOIN aula             aul ON aul.id_aula      = sh.id_aula
   LEFT JOIN grupo       g   ON g.id_grupo       = sh.id_grupo
   LEFT JOIN guardies    gu  ON gu.id_sessio      = sh.id_sessio
                        AND gu.fecha_guardia  = :fecha
   LEFT JOIN docent      dc  ON dc.id_docent     = gu.docent_assignat
       WHERE au.fecha_ausencia  = :fecha
         AND sh.hora_desde BETWEEN CAST('06:00' AS time) AND CAST('23:00' AS time)
       ORDER BY sh.hora_desde DESC
    """, nativeQuery = true)
    List<SessionGuardiaDTO> findGuardiasDelDia(@Param("fecha") LocalDate fecha);

    //recuopera un ausente vinculado a la sesion pero solo 1 para evitar errores que no se ni como mapear  
    @Query(value = """
      SELECT d.*
        FROM ausencies_sessio s
        JOIN ausencies a ON a.id_ausencia = s.id_ausencia
        JOIN docent   d ON d.id_docent    = a.id_docent
       WHERE s.id_sessio      = :idSessio
         AND a.fecha_ausencia = :fecha
       LIMIT 1
    """, nativeQuery = true)
    Optional<Docent> findFirstDocentAbsentBySessionAndFecha(
        @Param("idSessio") Integer idSessio,
        @Param("fecha")    LocalDate fecha
    );


    @Query("""
       SELECT s.session
         FROM AusenciaSessio s
        WHERE s.ausencia.fechaAusencia = :fecha
    """)
    List<SessionHorario> findSessionsByFecha(@Param("fecha") LocalDate fecha);

    @Query("""
       SELECT s.ausencia.docent
         FROM AusenciaSessio s
        WHERE s.session.idSessio       = :idSessio
          AND s.ausencia.fechaAusencia = :fecha
    """)
    Optional<Docent> findDocentAbsentBySessionAndFecha(
        @Param("idSessio") Integer idSessio,
        @Param("fecha")    LocalDate fecha
    );
    
    //me quiero suicidar te odio pero te amo sql
}
