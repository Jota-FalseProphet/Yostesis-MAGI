// src/main/java/com/magi/api/model/AusenciaSessioId.java
package com.magi.api.model;

import java.io.Serializable;
import java.util.Objects;

public class AusenciaSessioId implements Serializable {
    private Integer idAusencia;
    private Integer idSessio;

    public AusenciaSessioId() {}
    public AusenciaSessioId(Integer idAusencia, Integer idSessio) {
        this.idAusencia = idAusencia;
        this.idSessio   = idSessio;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AusenciaSessioId)) return false;
        AusenciaSessioId that = (AusenciaSessioId) o;
        return Objects.equals(idAusencia, that.idAusencia)
            && Objects.equals(idSessio,   that.idSessio);
    }
    @Override public int hashCode() {
        return Objects.hash(idAusencia, idSessio);
    }
}
