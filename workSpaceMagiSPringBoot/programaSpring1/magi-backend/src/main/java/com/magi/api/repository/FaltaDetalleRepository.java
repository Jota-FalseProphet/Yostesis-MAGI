package com.magi.api.repository;

import com.magi.api.model.FaltaDetalle;
import com.magi.api.model.FaltaDetalleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FaltaDetalleRepository
        extends JpaRepository<FaltaDetalle, FaltaDetalleId> {

    List<FaltaDetalle> findByFecha(LocalDate fecha);

    List<FaltaDetalle> findByFechaBetween(LocalDate desde, LocalDate hasta);

    List<FaltaDetalle> findByIdDocenteAndFechaBetween(
            Integer idDocente, LocalDate desde, LocalDate hasta);

    List<FaltaDetalle> findByIdGrupoAndFechaBetween(
            Integer idGrupo, LocalDate desde, LocalDate hasta);

    @Query("""
           SELECT f
             FROM FaltaDetalle f
            WHERE (f.fecha >= COALESCE(:desde, f.fecha))
              AND (f.fecha <= COALESCE(:hasta, f.fecha))
              AND (:idDocente IS NULL OR f.idDocente = :idDocente)
              AND (:idGrupo   IS NULL OR f.idGrupo   = :idGrupo)
           """)
    List<FaltaDetalle> buscarDetalle(
            @Param("desde")     LocalDate desde,
            @Param("hasta")     LocalDate hasta,
            @Param("idDocente") Integer   idDocente,
            @Param("idGrupo")   Integer   idGrupo);
}
