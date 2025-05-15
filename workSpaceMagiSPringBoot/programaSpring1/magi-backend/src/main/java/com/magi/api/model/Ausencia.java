// src/main/java/com/magi/api/model/Ausencia.java
package com.magi.api.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "ausencies")
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ausencia")
    private Integer idAusencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_docent", referencedColumnName = "id_docent")
    private Docent docent;

    @Column(name = "fecha_ausencia", nullable = false)
    private LocalDate fechaAusencia;

    @Column(name = "is_full_day")
    private Boolean fullDay;

    public Ausencia() {}

    public Integer getIdAusencia() {
        return idAusencia;
    }
    public void setIdAusencia(Integer idAusencia) {
        this.idAusencia = idAusencia;
    }

    public Docent getDocent() {
        return docent;
    }
    public void setDocent(Docent docent) {
        this.docent = docent;
    }

    public LocalDate getFechaAusencia() {
        return fechaAusencia;
    }
    public void setFechaAusencia(LocalDate fechaAusencia) {
        this.fechaAusencia = fechaAusencia;
    }

    public Boolean getFullDay() {
        return fullDay;
    }
    public void setFullDay(Boolean fullDay) {
        this.fullDay = fullDay;
    }
}
