package com.magi.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.magi.api.model.Fichaje;

@Repository
public interface FichajeRepository extends JpaRepository<Fichaje, Long> {
	List<Fichaje> findByDniOrderByFechaHoraDesc(String dni);
}

