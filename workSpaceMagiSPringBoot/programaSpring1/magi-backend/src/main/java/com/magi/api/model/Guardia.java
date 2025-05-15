package com.magi.api.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "guardies")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Guardia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guardia")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docent_assignat", referencedColumnName = "id_docent")
    private Docent docentAssignat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docent_absent", referencedColumnName = "id_docent")
    private Docent docentAbsent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sessio", referencedColumnName = "id_sessio")
    private SessionHorario session;

    @Column(name = "fecha_guardia", nullable = false)
    private LocalDate fechaGuardia;

    protected Guardia() {}

    public Guardia(Docent docentAssignat, Docent docentAbsent, SessionHorario session) {
        this.docentAssignat = docentAssignat;
        this.docentAbsent   = docentAbsent;
        this.session        = session;
        this.fechaGuardia   = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Docent getDocentAssignat() {
        return docentAssignat;
    }

    public void setDocentAssignat(Docent docentAssignat) {
        this.docentAssignat = docentAssignat;
    }

    public Docent getDocentAbsent() {
        return docentAbsent;
    }

    public void setDocentAbsent(Docent docentAbsent) {
        this.docentAbsent = docentAbsent;
    }

    public SessionHorario getSession() {
        return session;
    }

    public void setSession(SessionHorario session) {
        this.session = session;
    }

    public LocalDate getFechaGuardia() {
        return fechaGuardia;
    }

    public void setFechaGuardia(LocalDate fechaGuardia) {
        this.fechaGuardia = fechaGuardia;
    }
}
