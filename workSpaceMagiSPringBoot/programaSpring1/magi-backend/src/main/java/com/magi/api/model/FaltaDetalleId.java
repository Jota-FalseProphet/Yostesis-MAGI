package com.magi.api.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta para FaltaDetalle.
 */
public class FaltaDetalleId implements Serializable {

    private Long idAusencia;
    private Integer idSessio;

    public FaltaDetalleId() {
    }

    public FaltaDetalleId(Long idAusencia, Integer idSessio) {
        this.idAusencia = idAusencia;
        this.idSessio = idSessio;
    }

    public Long getIdAusencia() {
        return idAusencia;
    }

    public void setIdAusencia(Long idAusencia) {
        this.idAusencia = idAusencia;
    }

    public Integer getIdSessio() {
        return idSessio;
    }

    public void setIdSessio(Integer idSessio) {
        this.idSessio = idSessio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaltaDetalleId)) return false;
        FaltaDetalleId that = (FaltaDetalleId) o;
        return Objects.equals(idAusencia, that.idAusencia) &&
               Objects.equals(idSessio, that.idSessio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAusencia, idSessio);
    }

    @Override
    public String toString() {
        return "FaltaDetalleId{" +
               "idAusencia=" + idAusencia +
               ", idSessio=" + idSessio +
               '}';
    }
}
