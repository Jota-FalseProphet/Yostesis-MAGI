package com.magi.api.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;


@Entity
@Table(name = "vw_faltas")
@org.hibernate.annotations.Immutable
@IdClass(FaltaDetalleId.class)
public class FaltaDetalle implements Serializable {

    @Id
    @Column(name = "id_ausencia")
    private Long idAusencia;

    @Id
    @Column(name = "id_sessio")
    private Integer idSessio;

    private LocalDate fecha;

    @Column(name = "id_docente")
    private Integer idDocente;

    private String docente;

    @Column(name = "id_guardia")
    private Long idGuardia;

    @Column(name = "docent_assignat")
    private Integer docentAssignat;

    @Column(name = "docente_guardia")
    private String docenteGuardia;

    @Column(name = "id_grupo")
    private Integer idGrupo;

    private String grupo;

    @Column(name = "hora_desde")
    private LocalTime horaDesde;

    @Column(name = "hora_fins")
    private LocalTime horaFins;

    public FaltaDetalle() {
    }

    public FaltaDetalle(Long idAusencia, Integer idSessio, LocalDate fecha, Integer idDocente, String docente,
                        Long idGuardia, Integer docentAssignat, String docenteGuardia,
                        Integer idGrupo, String grupo, LocalTime horaDesde, LocalTime horaFins) {
        this.idAusencia = idAusencia;
        this.idSessio = idSessio;
        this.fecha = fecha;
        this.idDocente = idDocente;
        this.docente = docente;
        this.idGuardia = idGuardia;
        this.docentAssignat = docentAssignat;
        this.docenteGuardia = docenteGuardia;
        this.idGrupo = idGrupo;
        this.grupo = grupo;
        this.horaDesde = horaDesde;
        this.horaFins = horaFins;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(Integer idDocente) {
        this.idDocente = idDocente;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public Long getIdGuardia() {
        return idGuardia;
    }

    public void setIdGuardia(Long idGuardia) {
        this.idGuardia = idGuardia;
    }

    public Integer getDocentAssignat() {
        return docentAssignat;
    }

    public void setDocentAssignat(Integer docentAssignat) {
        this.docentAssignat = docentAssignat;
    }

    public String getDocenteGuardia() {
        return docenteGuardia;
    }

    public void setDocenteGuardia(String docenteGuardia) {
        this.docenteGuardia = docenteGuardia;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(LocalTime horaDesde) {
        this.horaDesde = horaDesde;
    }

    public LocalTime getHoraFins() {
        return horaFins;
    }

    public void setHoraFins(LocalTime horaFins) {
        this.horaFins = horaFins;
    }

    // ---------- Derived ----------
    @Transient
    public boolean isCubierta() {
        return this.idGuardia != null;
    }

    // ---------- equals & hashCode ----------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaltaDetalle)) return false;
        FaltaDetalle that = (FaltaDetalle) o;
        return Objects.equals(idAusencia, that.idAusencia) &&
               Objects.equals(idSessio, that.idSessio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAusencia, idSessio);
    }

    // ---------- toString ----------
    @Override
    public String toString() {
        return "FaltaDetalle{" +
               "idAusencia=" + idAusencia +
               ", idSessio=" + idSessio +
               ", fecha=" + fecha +
               ", idDocente=" + idDocente +
               ", docente='" + docente + '\'' +
               ", idGuardia=" + idGuardia +
               ", docentAssignat=" + docentAssignat +
               ", docenteGuardia='" + docenteGuardia + '\'' +
               ", idGrupo=" + idGrupo +
               ", grupo='" + grupo + '\'' +
               ", horaDesde=" + horaDesde +
               ", horaFins=" + horaFins +
               '}';
    }
}
