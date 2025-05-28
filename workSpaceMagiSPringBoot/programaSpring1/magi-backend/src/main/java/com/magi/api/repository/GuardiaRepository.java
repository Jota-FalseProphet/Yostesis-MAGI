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

    /**
     * 1) Si ya existe una fila en guardies para (id_sessio, fechaGuardia),
     *    actualiza docentAssignat y devuelve el número de filas afectadas.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Guardia g
           SET g.docentAssignat = :docentAsignat
         WHERE g.session.idSessio   = :idSessio
           AND g.fechaGuardia       = :fechaGuardia
        """)
    int asignarGuardia(
        @Param("docentAsignat") Docent   docentAsignat,
        @Param("idSessio")       Integer idSessio,
        @Param("fechaGuardia")   LocalDate fechaGuardia
    );

    /**
     * 2) Inserta directamente una nueva guardia en la tabla.
     *    Úsalo cuando el UPDATE anterior devolvió 0 filas.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO guardies
          (docent_assignat, docent_absent, id_sessio, fecha_guardia)
        VALUES
          (:docentAsignat, :docentAbsent, :idSessio, :fechaGuardia)
        """, nativeQuery = true)
    void insertarGuardia(
        @Param("docentAsignat") Docent     docentAsignat,
        @Param("docentAbsent")   Docent     docentAbsent,
        @Param("idSessio")       Integer    idSessio,
        @Param("fechaGuardia")   LocalDate  fechaGuardia
    );

}
