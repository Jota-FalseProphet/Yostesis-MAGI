package com.magi.api.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.CrudRepository;

import com.magi.api.model.Guardia;

public interface GuardiaRepository extends CrudRepository<Guardia, Long> {

    /**
     * Inserta o actualiza en un solo paso (INSERT ... ON CONFLICT).
     */
    @Modifying
    @Transactional
    @Query(value =
        "INSERT INTO public.guardies " +
        "  (docent_assignat, docent_absent, id_sessio, fecha_guardia) " +
        "VALUES " +
        "  (:docentAsignatId, :docentAbsentId, :idSessio, :fechaGuardia) " +
        "ON CONFLICT (id_sessio, fecha_guardia) " +
        "  DO UPDATE SET docent_assignat = EXCLUDED.docent_assignat",
        nativeQuery = true
    )
    void cubrir(
        @Param("docentAsignatId") Long     docentAsignatId,
        @Param("docentAbsentId")   Long     docentAbsentId,
        @Param("idSessio")         Integer  idSessio,
        @Param("fechaGuardia")     LocalDate fechaGuardia
    );
}
