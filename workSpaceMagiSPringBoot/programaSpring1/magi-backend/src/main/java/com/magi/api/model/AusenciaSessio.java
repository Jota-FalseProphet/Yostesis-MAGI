package com.magi.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ausencies_sessio")
public class AusenciaSessio {

    @EmbeddedId
    private AusenciaSessioId id;

    @MapsId("idAusencia")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ausencia", nullable = false)
    private Ausencia ausencia;

    @MapsId("idSessio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sessio", nullable = false)
    private SessionHorario session;

    public AusenciaSessio() {}

    public AusenciaSessio(AusenciaSessioId id, Ausencia ausencia, SessionHorario session) {
        this.id = id;
        this.ausencia = ausencia;
        this.session = session;
    }

    public AusenciaSessioId getId() {
        return id;
    }

    public void setId(AusenciaSessioId id) {
        this.id = id;
    }

    public Ausencia getAusencia() {
        return ausencia;
    }

    public void setAusencia(Ausencia ausencia) {
        this.ausencia = ausencia;
    }

    public SessionHorario getSession() {
        return session;
    }

    public void setSession(SessionHorario session) {
        this.session = session;
    }
}
