// src/main/java/com/magi/api/repository/AusenciaSessioRepository.java
package com.magi.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.magi.api.model.*;

public interface AusenciaSessioRepository
    extends JpaRepository<AusenciaSessio, AusenciaSessioId> {

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
        @Param("fecha") LocalDate fecha
    );
}
