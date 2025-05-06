package com.magi.api.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.magi.api.model.Fichaje;
import com.magi.api.model.Usuario;

public interface FichajeRepository extends JpaRepository<Fichaje, Long> {
    Optional<Fichaje> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
}
