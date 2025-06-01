package com.magi.api.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import com.magi.api.model.Ausencia;

public interface AusenciaRepository extends JpaRepository<Ausencia, Integer> {
    boolean existsByDocent_IdDocentAndFechaAusencia(int idDocent, LocalDate fechaAusencia);
}
