package com.magi.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.magi.api.model.SessionHorario;

public interface SessionHorarioRepository extends JpaRepository<SessionHorario, Integer> {

    /**
     * Devuelve las sesiones (tramos) que imparte el docente con ID dado en el día de la semana indicado.
     * Hacemos JOIN sobre DocentSessio y devolvemos ds.session.
     */
    @Query("""
        SELECT ds.session
          FROM DocentSessio ds
         WHERE ds.docent.idDocent = :docenteId
           AND ds.session.diaSetmana = :diaSemana
         ORDER BY ds.session.horaDesde
    """)
    List<SessionHorario> findByDocenteAndDia(
        @Param("docenteId") Integer docenteId,
        @Param("diaSemana") String diaSemana
    );
}
