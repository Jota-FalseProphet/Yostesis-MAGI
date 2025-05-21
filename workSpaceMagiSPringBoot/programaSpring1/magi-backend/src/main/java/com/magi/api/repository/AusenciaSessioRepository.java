// src/main/java/com/magi/api/repository/AusenciaSessioRepository.java
package com.magi.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.*;

public interface AusenciaSessioRepository
    extends JpaRepository<AusenciaSessio, AusenciaSessioId> {

    /** 
     * Devuelve cada sesión con ausencia y, si existe, el guardián asignado
     * mapeado al DTO SessionGuardiaDTO 
     */
    @Query("""
        SELECT new com.magi.api.dto.SessionGuardiaDTO(
            s.session.idSessio,
            s.session.diaSetmana,
            s.session.horaDesde,
            s.ausencia.docent.dni,
            concat(s.ausencia.docent.nom, ' ', s.ausencia.docent.cognom1),
            coalesce(g.docentAssignat.dni, ''),
            coalesce(concat(g.docentAssignat.nom, ' ', g.docentAssignat.cognom1), '')
        )
        FROM AusenciaSessio s
        LEFT JOIN Guardia g
            ON g.session      = s.session
           AND g.docentAbsent = s.ausencia.docent
           AND g.fechaGuardia = s.ausencia.fechaAusencia
        WHERE s.ausencia.fechaAusencia = :fecha
        ORDER BY s.session.horaDesde
    """)
    List<SessionGuardiaDTO> findGuardiasVigentes(@Param("fecha") LocalDate fecha);

    /** — tus otros métodos antiguos — */

    @Query("""
        SELECT s.session
          FROM AusenciaSessio s
         WHERE s.ausencia.fechaAusencia = :fecha
    """)
    List<SessionHorario> findSessionsByFecha(@Param("fecha") LocalDate fecha);

    @Query("""
        SELECT s.ausencia.docent
          FROM AusenciaSessio s
         WHERE s.idSessio = :idSessio
           AND s.ausencia.fechaAusencia = :fecha
    """)
    Optional<Docent> findDocentAbsentBySessionAndFecha(
        @Param("idSessio") Integer idSessio,
        @Param("fecha")    LocalDate fecha
    );
}
