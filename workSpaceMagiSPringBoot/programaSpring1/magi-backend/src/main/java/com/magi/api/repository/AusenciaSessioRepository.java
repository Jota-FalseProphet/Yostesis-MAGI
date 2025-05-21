package com.magi.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.model.AusenciaSessio;
import com.magi.api.model.AusenciaSessioId;
import com.magi.api.model.DocentSessio;
import com.magi.api.model.SessionHorario;
import com.magi.api.model.Docent;

public interface AusenciaSessioRepository
    extends JpaRepository<AusenciaSessio, AusenciaSessioId> {

    /** 
     * Devuelve cada sesión con ausencia y, si existe, el guardián asignado
     * mapeado al DTO SessionGuardiaDTO, filtrando sólo sesiones sin codocencia
     */
    @Query("""
      SELECT new com.magi.api.dto.SessionGuardiaDTO(
        s.session.idSessio,
        s.session.diaSetmana,
        s.session.horaDesde,
        s.session.horaFins,
        s.session.plantilla,
        s.session.aula.nombre,
        s.ausencia.docent.dni,
        s.ausencia.docent.nom,
        NULL,
        NULL
      )
      FROM AusenciaSessio s
      WHERE s.ausencia.fechaAusencia = :fecha
        AND s.ausencia.fullDay = false
        AND (
          SELECT COUNT(ds)
            FROM DocentSessio ds
           WHERE ds.session.idSessio = s.session.idSessio
        ) = 1
    """)
    List<SessionGuardiaDTO> findGuardiasVigentes(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene las sesiones programadas para un día dado
     */
    @Query("""
        SELECT s.session
          FROM AusenciaSessio s
         WHERE s.ausencia.fechaAusencia = :fecha
    """)
    List<SessionHorario> findSessionsByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene el docent ausente para una sesión en una fecha concreta
     */
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