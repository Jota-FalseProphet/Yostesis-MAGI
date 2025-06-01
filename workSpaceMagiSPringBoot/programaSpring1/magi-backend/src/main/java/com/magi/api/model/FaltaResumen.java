package com.magi.api.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


@Entity
@Table(name = "vw_faltas_resumen")
@org.hibernate.annotations.Immutable
@IdClass(FaltaResumenId.class)
public class FaltaResumen implements Serializable {

    private static final long serialVersionUID = 1L;

  
    @Id
    @Column(name = "mes")
    private LocalDate mes;   

    @Id
    @Column(name = "id_docente")
    private Integer idDocente;

    @Column(name = "sesiones_faltadas")
    private Long sesionesFaltadas;

    @Column(name = "sesiones_cubiertas")
    private Long sesionesCubiertas;
    public FaltaResumen() {
    }

    public FaltaResumen(LocalDate mes,
                        Integer idDocente,
                        Long sesionesFaltadas,
                        Long sesionesCubiertas) {
        this.mes = mes;
        this.idDocente = idDocente;
        this.sesionesFaltadas = sesionesFaltadas;
        this.sesionesCubiertas = sesionesCubiertas;
    }

    public LocalDate getMes() {
        return mes;
    }

    public void setMes(LocalDate mes) {
        this.mes = mes;
    }

    public Integer getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Integer idDocente) {
        this.idDocente = idDocente;
    }

    public Long getSesionesFaltadas() {
        return sesionesFaltadas;
    }

    public void setSesionesFaltadas(Long sesionesFaltadas) {
        this.sesionesFaltadas = sesionesFaltadas;
    }

    public Long getSesionesCubiertas() {
        return sesionesCubiertas;
    }

    public void setSesionesCubiertas(Long sesionesCubiertas) {
        this.sesionesCubiertas = sesionesCubiertas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaltaResumen)) return false;
        FaltaResumen that = (FaltaResumen) o;
        return Objects.equals(mes, that.mes) &&
               Objects.equals(idDocente, that.idDocente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mes, idDocente);
    }

    @Override
    public String toString() {
        return "FaltaResumen{" +
               "mes=" + mes +
               ", idDocente=" + idDocente +
               ", sesionesFaltadas=" + sesionesFaltadas +
               ", sesionesCubiertas=" + sesionesCubiertas +
               '}';
    }
}
