package com.magi.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.magi.api.model.Guardia;
import com.magi.api.dto.GuardiaHistoricoDTO;

public interface GuardiaRepository extends JpaRepository<Guardia, Long> {

    /**
     * Inserta o actualiza en un solo paso (INSERT ... ON CONFLICT).
     */
    @Modifying
    @Transactional
    @Query(value =
        "INSERT INTO public.guardies " +
        "  (docent_assignat, docent_absent, id_sessio, fecha_guardia) " +
        "VALUES " +
        "  (:docentAsignatId, :docentAbsentId, :idSessio, :fechaGuardia) " +
        "ON CONFLICT (id_sessio, fecha_guardia) " +
        "  DO UPDATE SET docent_assignat = EXCLUDED.docent_assignat",
        nativeQuery = true
    )
    void cubrir(
        @Param("docentAsignatId") Long     docentAsignatId,
        @Param("docentAbsentId")   Long     docentAbsentId,
        @Param("idSessio")         Integer  idSessio,
        @Param("fechaGuardia")     LocalDate fechaGuardia
    );

    /**
     * Devuelve todas las guardias como entidades Guardia.
     */
    List<Guardia> findByDocentAssignatDni(String dni);

    /**
     * Histórico completo de guardias de un docente (por DNI), mapeado al DTO
     * con sesión y franja horaria (desde–fins) correctas.
     */
    @Query(value = """
      SELECT 
        g.id_guardia::bigint                     AS id,
        da.document                           AS dniAsignat,
        db.document                           AS dniAbsent,
        gr.nombre                    AS grupo,
        COALESCE(au.nombre, '')          AS aula,
        g.fecha_guardia                  AS fechaGuardia,
        sh.sessio_ordre                  AS sessionId,
        to_char(sh.hora_desde,'HH24:MI')
          || '-' ||
        to_char(sh.hora_fins,  'HH24:MI') AS hora
      FROM public.guardies g
      JOIN public.docent da   ON da.id_docent = g.docent_assignat
      JOIN public.docent db   ON db.id_docent = g.docent_absent
      JOIN public.sessions_horari sh ON sh.id_sessio = g.id_sessio
      JOIN public.grupo gr     ON gr.id_grupo = sh.id_grupo
      LEFT JOIN public.aula au ON au.id_aula = sh.id_aula
      WHERE da.document  = :dni
      ORDER BY g.fecha_guardia DESC, sh.sessio_ordre
    """, nativeQuery = true)
    List<GuardiaHistoricoDTO> findHistoricoNativePorDni(@Param("dni") String dni);

    /**
     * Histórico completo de todas las guardias (para administrador).
     */
    @Query(value = """
      SELECT 
        g.id_guardia::bigint                     AS id,
        da.document                           AS dniAsignat,
        db.document                           AS dniAbsent,
        gr.nombre                    AS grupo,
        COALESCE(au.nombre, '')          AS aula,
        g.fecha_guardia                  AS fechaGuardia,
        sh.sessio_ordre                  AS sessionId,
        to_char(sh.hora_desde,'HH24:MI')
          || '-' ||
        to_char(sh.hora_fins,  'HH24:MI') AS hora
      FROM public.guardies g
      JOIN public.docent da   ON da.id_docent = g.docent_assignat
      JOIN public.docent db   ON db.id_docent = g.docent_absent
      JOIN public.sessions_horari sh ON sh.id_sessio = g.id_sessio
      JOIN public.grupo gr     ON gr.id_grupo = sh.id_grupo
      LEFT JOIN public.aula au ON au.id_aula = sh.id_aula
      ORDER BY g.fecha_guardia DESC, sh.sessio_ordre
    """, nativeQuery = true)
    List<GuardiaHistoricoDTO> findHistoricoNativeAll();

}
