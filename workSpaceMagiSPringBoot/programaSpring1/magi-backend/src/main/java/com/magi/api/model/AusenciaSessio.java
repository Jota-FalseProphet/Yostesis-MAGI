// src/main/java/com/magi/api/model/AusenciaSessio.java
package com.magi.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ausencies_sessio")
@IdClass(AusenciaSessioId.class)
public class AusenciaSessio {

    @Id
    @Column(name = "id_ausencia")
    private Integer idAusencia;

    @Id
    @Column(name = "id_sessio")
    private Integer idSessio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ausencia", insertable = false, updatable = false)
    private Ausencia ausencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessio", insertable = false, updatable = false)
    private SessionHorario session;

    public AusenciaSessio() {}

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

    public Ausencia getAusencia() {
        return ausencia;
    }
    public SessionHorario getSession() {
        return session;
    }
}
