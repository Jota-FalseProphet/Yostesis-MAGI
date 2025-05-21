// src/main/java/com/magi/api/dto/SessionGuardiaDTO.java
package com.magi.api.dto;

import java.time.LocalTime;

public class SessionGuardiaDTO {
    private Integer idSessio;        // <-- de Long a Integer
    private String  diaSetmana;
    private LocalTime horaDesde;
    private String  absenteDni;
    private String  absenteNombre;
    private String  sustitutoDni;
    private String  sustitutoNombre;

    public SessionGuardiaDTO() {}

    public SessionGuardiaDTO(
        Integer idSessio,           // <-- Integer aquí
        String  diaSetmana,
        LocalTime horaDesde,
        String  absenteDni,
        String  absenteNombre,
        String  sustitutoDni,
        String  sustitutoNombre
    ) {
        this.idSessio        = idSessio;
        this.diaSetmana      = diaSetmana;
        this.horaDesde       = horaDesde;
        this.absenteDni      = absenteDni;
        this.absenteNombre   = absenteNombre;
        this.sustitutoDni    = sustitutoDni;
        this.sustitutoNombre = sustitutoNombre;
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

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(LocalTime horaDesde) {
        this.horaDesde = horaDesde;
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

    public String getSustitutoDni() {
        return sustitutoDni;
    }

    public void setSustitutoDni(String sustitutoDni) {
        this.sustitutoDni = sustitutoDni;
    }

    public String getSustitutoNombre() {
        return sustitutoNombre;
    }

    public void setSustitutoNombre(String sustitutoNombre) {
        this.sustitutoNombre = sustitutoNombre;
    }
}
