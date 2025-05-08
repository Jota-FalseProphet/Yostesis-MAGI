package com.magi.api.repository;

import com.magi.api.model.Fichaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichajeRepository extends JpaRepository<Fichaje, Long> {

    /**
     * Busca la primera jornada iniciada (horaFin == null)
     * para el usuario identificado por su DNI.
     */
    Optional<Fichaje> findFirstByUsuarioDniAndHoraFinIsNull(String dni);

}
