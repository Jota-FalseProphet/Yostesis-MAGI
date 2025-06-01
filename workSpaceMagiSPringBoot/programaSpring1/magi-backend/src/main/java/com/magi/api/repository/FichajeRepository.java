package com.magi.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.magi.api.model.Fichaje;
import com.magi.api.model.Usuario;

public interface FichajeRepository extends JpaRepository<Fichaje, Long> {
	boolean existsByUsuarioAndFecha(Usuario usuario, LocalDate fecha);

    // todos los fichajes del usuario en esa fecha
    List<Fichaje> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);

    // el tramo abierto más reciente para hoy
    Optional<Fichaje> findTopByUsuarioAndFechaAndHoraFinIsNullOrderByHoraInicioDesc(
        Usuario usuario,
        LocalDate fecha
    );
}
