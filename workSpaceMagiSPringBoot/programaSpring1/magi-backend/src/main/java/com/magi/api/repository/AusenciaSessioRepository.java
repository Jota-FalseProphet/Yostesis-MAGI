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
			    s.session.horaHasta,                
			    s.session.grup.nom,                  
			    s.session.aula.nom,                  
			    s.ausencia.docent.dni,
			    s.ausencia.docent.nom,
			    NULL,
			    NULL
			  )
			  FROM AusenciaSessio s
			  WHERE s.ausencia.fechaAusencia = :fecha
			    AND s.ausencia.fullDay = false
			    AND s.session.codocent IS NULL
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
