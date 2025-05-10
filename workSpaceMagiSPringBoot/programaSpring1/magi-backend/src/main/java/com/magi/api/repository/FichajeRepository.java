// FichajeRepository.java
package com.magi.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.magi.api.model.Fichaje;
import com.magi.api.model.Usuario;

public interface FichajeRepository extends JpaRepository<Fichaje, Long> {

    // Todos los fichajes del usuario en esa fecha
    List<Fichaje> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);

    // El tramo abierto más reciente (sin horaFin) para hoy
    Optional<Fichaje> findTopByUsuarioAndFechaAndHoraFinIsNullOrderByHoraInicioDesc(
        Usuario usuario,
        LocalDate fecha
    );
}
