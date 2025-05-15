package com.magi.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.magi.api.model.HorarioDocenteSesion;

public interface HorarioDocenteSesionRepository
        extends JpaRepository<HorarioDocenteSesion, Integer> {

    List<HorarioDocenteSesion> findByDiaSemana(String diaSemana);
}
