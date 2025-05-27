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

	@Query(value = """
		    SELECT sh.id_sessio,
		           sh.dia_setmana,
		           sh.hora_desde,
		           sh.hora_fins,
		           COALESCE(g.nombre, sh.plantilla) AS grupo,
		           aul.nombre                       AS aula,
		           d.document                       AS absenteDni,
		           d.nom                            AS absenteNom,
		           CASE
		             WHEN EXISTS (
		               SELECT 1
		                 FROM guardies gu
		                WHERE gu.id_sessio    = sh.id_sessio
		                  AND gu.fecha_guardia = :fecha
		                  AND gu.docent_assignat IS NOT NULL
		             )
		             THEN TRUE
		             ELSE FALSE
		           END                               AS cubierta,
		           (
		             SELECT dc.nom
		               FROM guardies gu
		               JOIN docent dc ON dc.id_docent = gu.docent_assignat
		              WHERE gu.id_sessio     = sh.id_sessio
		                AND gu.fecha_guardia  = :fecha
		                AND gu.docent_assignat IS NOT NULL
		              LIMIT 1
		           )                                 AS profesorGuardia
		      FROM ausencies_sessio ass
		      JOIN sessions_horari  sh  ON sh.id_sessio  = ass.id_sessio
		      JOIN ausencies        au  ON au.id_ausencia = ass.id_ausencia
		      JOIN docent           d   ON d.id_docent    = au.id_docent
		      JOIN aula             aul ON aul.id_aula     = sh.id_aula
		      LEFT JOIN grupo       g   ON g.id_grupo      = sh.id_grupo
		     WHERE au.fecha_ausencia     = :fecha
		       AND au.is_full_day        = FALSE
		       AND :ahora BETWEEN (
		             sh.hora_desde + make_interval(mins => :graciaMin)
		           ) AND sh.hora_fins
		       AND (
		             SELECT COUNT(*)
		               FROM docent_sessio ds
		              WHERE ds.id_sessio = sh.id_sessio
		           ) = 1
		     ORDER BY sh.hora_desde DESC
		""", nativeQuery = true)
		List<SessionGuardiaDTO> findGuardiasVigentes(
		    @Param("fecha")     LocalDate fecha,
		    @Param("ahora")     LocalTime ahora,
		    @Param("graciaMin") int       graciaMin);


    /* ------------------------------------------------------------- */

    @Query("""
           SELECT s.session
             FROM AusenciaSessio s
            WHERE s.ausencia.fechaAusencia = :fecha
           """)
    List<SessionHorario> findSessionsByFecha(@Param("fecha") LocalDate fecha);

    @Query("""
           SELECT s.ausencia.docent
             FROM AusenciaSessio s
            WHERE s.session.idSessio      = :idSessio
              AND s.ausencia.fechaAusencia = :fecha
           """)
    Optional<Docent> findDocentAbsentBySessionAndFecha(
            @Param("idSessio") Integer idSessio,
            @Param("fecha")    LocalDate fecha);
}
