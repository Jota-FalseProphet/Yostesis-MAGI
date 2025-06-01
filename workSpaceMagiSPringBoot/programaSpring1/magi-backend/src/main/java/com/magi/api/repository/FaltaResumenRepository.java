package com.magi.api.repository;

import com.magi.api.model.FaltaResumen;
import com.magi.api.model.FaltaResumenId;           // ← importa la PK
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface FaltaResumenRepository
        extends JpaRepository<FaltaResumen, FaltaResumenId> {  

    //resumen del mes solo primer dia del mes
    List<FaltaResumen> findByMes(LocalDate mesPrimerDia);

   //rango de meses incluyendo amgos
    List<FaltaResumen> findByMesBetween(LocalDate desdeMes, LocalDate hastaMes);

    //resumen de un docente dado un mes
    List<FaltaResumen> findByIdDocenteAndMesBetween(
            Integer idDocente, LocalDate desdeMes, LocalDate hastaMes);

    //consulta flexible para mes curso o semana
    @Query("""
           SELECT r
             FROM FaltaResumen r
            WHERE (:desdeMes  IS NULL OR r.mes       >= :desdeMes)
              AND (:hastaMes  IS NULL OR r.mes       <= :hastaMes)
              AND (:idDocente IS NULL OR r.idDocente =  :idDocente)
           """)
    List<FaltaResumen> buscarResumen(
            @Param("desdeMes")  LocalDate desdeMes,
            @Param("hastaMes")  LocalDate hastaMes,
            @Param("idDocente") Integer   idDocente);
}
