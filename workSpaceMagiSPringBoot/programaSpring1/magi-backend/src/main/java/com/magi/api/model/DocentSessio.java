package com.magi.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "docent_sessio")
public class DocentSessio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docent", nullable = false)
    private Docent docent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessio", nullable = false)
    private SessionHorario session;

    @Column(name = "ocupacion")
    private String ocupacion;

    public Integer getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(Integer idAsignacion) { this.idAsignacion = idAsignacion; }

    public Docent getDocent() { return docent; }
    public void setDocent(Docent docent) { this.docent = docent; }

    public SessionHorario getSession() { return session; }
    public void setSession(SessionHorario session) { this.session = session; }

    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }
}
