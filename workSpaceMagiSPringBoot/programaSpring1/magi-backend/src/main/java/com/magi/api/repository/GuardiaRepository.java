package com.magi.api.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.magi.api.model.Docent;
import com.magi.api.model.Guardia;
import com.magi.api.model.SessionHorario;

public interface GuardiaRepository extends JpaRepository<Guardia, Long> {

    boolean existsByDocentAbsentAndSessionAndFechaGuardia(
        Docent docentAbsent,
        SessionHorario session,
        LocalDate fechaGuardia
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Guardia g
          SET g.docentAssignat = :docentAsignat
        WHERE g.session.idSessio     = :idSessio
          AND g.fechaGuardia         = :fechaGuardia
        """)
    int asignarGuardia(
        @Param("docentAsignat") Docent   docentAsignat,
        @Param("idSessio")       Integer idSessio,
        @Param("fechaGuardia")   LocalDate fechaGuardia
    );

 
    default Guardia crearGuardiaEnMemoria(
        Docent absent,
        SessionHorario session,
        LocalDate fechaGuardia,
        Docent asignat
    ) {
        Guardia g = new Guardia(asignat, absent, session);
        g.setFechaGuardia(fechaGuardia);
        return save(g);
    }
}
