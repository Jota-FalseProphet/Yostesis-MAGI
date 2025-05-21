// src/main/java/com/magi/api/dto/SessionGuardiaDTO.java
package com.magi.api.dto;

import java.time.LocalTime;

public class SessionGuardiaDTO {
    private Integer idSessio;        // <-- de Long a Integer
    private String  diaSetmana;
    private LocalTime horaDesde;
    private LocalTime horaHasta;     // ← nuevo
    private String grupo;            // ← nuevo
    private String aula;             // ← nuevo
    private String  absenteDni;
    private String  absenteNombre;
    private String  sustitutoDni;
    private String  sustitutoNombre;

    public SessionGuardiaDTO() {}

    public SessionGuardiaDTO(
        Integer idSessio,
        String  diaSetmana,
        LocalTime horaDesde,
        LocalTime horaHasta,
        String	grupo,
        String	aula,
        String  absenteDni,
        String  absenteNombre,
        String  sustitutoDni,
        String  sustitutoNombre
    ) {
        this.idSessio        = idSessio;
        this.diaSetmana      = diaSetmana;
        this.horaDesde       = horaDesde;
        this.horaHasta		 = horaHasta;
        this.grupo			 = grupo;
        this.aula			 = aula;
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

	public LocalTime getHoraHasta() {
		return horaHasta;
	}

	public void setHoraHasta(LocalTime horaHasta) {
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
}
