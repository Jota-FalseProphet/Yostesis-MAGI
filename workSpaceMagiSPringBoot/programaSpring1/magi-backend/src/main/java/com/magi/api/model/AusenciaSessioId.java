package com.magi.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AusenciaSessioId implements Serializable {

    @Column(name = "id_ausencia")
    private Integer idAusencia;

    @Column(name = "id_sessio")
    private Integer idSessio;

    public AusenciaSessioId() {}

    public AusenciaSessioId(Integer idAusencia, Integer idSessio) {
        this.idAusencia = idAusencia;
        this.idSessio   = idSessio;
    }

    public Integer getIdAusencia() {
        return idAusencia;
    }

    public void setIdAusencia(Integer idAusencia) {
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
        if (!(o instanceof AusenciaSessioId)) return false;
        AusenciaSessioId that = (AusenciaSessioId) o;
        return Objects.equals(idAusencia, that.idAusencia)
            && Objects.equals(idSessio,   that.idSessio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAusencia, idSessio);
    }
}
