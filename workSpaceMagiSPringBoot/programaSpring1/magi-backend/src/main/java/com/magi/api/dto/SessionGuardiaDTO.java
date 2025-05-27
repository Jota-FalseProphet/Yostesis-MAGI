package com.magi.api.dto;

import java.sql.Time;

public class SessionGuardiaDTO {

    private Integer idSessio;
    private String diaSetmana;
    private Time horaDesde;
    private Time horaHasta;
    private String grupo;
    private String aula;
    private String absenteDni;
    private String absenteNombre;
    private Boolean cubierta;
    private String profesorGuardia;

    public SessionGuardiaDTO() {
    }

    public SessionGuardiaDTO(Integer idSessio,
                             String diaSetmana,
                             Time horaDesde,
                             Time horaHasta,
                             String grupo,
                             String aula,
                             String absenteDni,
                             String absenteNombre,
                             Boolean cubierta,
                             String profesorGuardia) {
        this.idSessio = idSessio;
        this.diaSetmana = diaSetmana;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.grupo = grupo;
        this.aula = aula;
        this.absenteDni = absenteDni;
        this.absenteNombre = absenteNombre;
        this.cubierta = cubierta;
        this.profesorGuardia = profesorGuardia;
    }

    public Integer getIdSessio() {
        return idSessio;
    }

    public void setIdSessio(Integer idSessio) {
        this.idSessio = idSessio;
    }

    public String getDiaSetmana() {
        return diaSetmana;
    }

    public void setDiaSetmana(String diaSetmana) {
        this.diaSetmana = diaSetmana;
    }

    public Time getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(Time horaDesde) {
        this.horaDesde = horaDesde;
    }

    public Time getHoraHasta() {
        return horaHasta;
    }

    public void setHoraHasta(Time horaHasta) {
        this.horaHasta = horaHasta;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getAbsenteDni() {
        return absenteDni;
    }

    public void setAbsenteDni(String absenteDni) {
        this.absenteDni = absenteDni;
    }

    public String getAbsenteNombre() {
        return absenteNombre;
    }

    public void setAbsenteNombre(String absenteNombre) {
        this.absenteNombre = absenteNombre;
    }

    public Boolean getCubierta() {
        return cubierta;
    }

    public void setCubierta(Boolean cubierta) {
        this.cubierta = cubierta;
    }

    public String getProfesorGuardia() {
        return profesorGuardia;
    }

    public void setProfesorGuardia(String profesorGuardia) {
        this.profesorGuardia = profesorGuardia;
    }
}
