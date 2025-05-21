package com.magi.api.repository;


import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.magi.api.model.Docent;
import com.magi.api.model.Guardia;
import com.magi.api.model.SessionHorario;

public interface GuardiaRepository extends JpaRepository<Guardia, Long> {

    boolean existsByDocentAbsentAndSessionAndFechaGuardia(
        Docent docentAbsent,
        SessionHorario session,
        LocalDate fechaGuardia
    );

}
