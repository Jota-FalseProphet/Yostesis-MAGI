package com.magi.api.repository;

import com.magi.api.model.FaltaResumen;
import com.magi.api.model.FaltaResumenId;           // ← importa la PK
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Acceso de solo lectura a la vista vw_faltas_resumen (agregados).
 */
public interface FaltaResumenRepository
        extends JpaRepository<FaltaResumen, FaltaResumenId> {  // ← aquí ya NO es Void

    /* Resumen de un mes concreto (primer día del mes) */
    List<FaltaResumen> findByMes(LocalDate mesPrimerDia);

    /* Rango de meses (ambos inclusive) */
    List<FaltaResumen> findByMesBetween(LocalDate desdeMes, LocalDate hastaMes);

    /* Resumen de un docente en un rango dado */
    List<FaltaResumen> findByIdDocenteAndMesBetween(
            Integer idDocente, LocalDate desdeMes, LocalDate hastaMes);

    /* Consulta flexible (mes, trimestre o curso) */
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
