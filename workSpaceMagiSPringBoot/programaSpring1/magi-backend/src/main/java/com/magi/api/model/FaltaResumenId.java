package com.magi.api.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class FaltaResumenId implements Serializable {

    private LocalDate mes;
    private Integer   idDocente;

    public FaltaResumenId() {
    }

    public FaltaResumenId(LocalDate mes, Integer idDocente) {
        this.mes = mes;
        this.idDocente = idDocente;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaltaResumenId)) return false;
        FaltaResumenId that = (FaltaResumenId) o;
        return Objects.equals(mes, that.mes) &&
               Objects.equals(idDocente, that.idDocente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mes, idDocente);
    }

    @Override
    public String toString() {
        return "FaltaResumenId{" +
               "mes=" + mes +
               ", idDocente=" + idDocente +
               '}';
    }
}
