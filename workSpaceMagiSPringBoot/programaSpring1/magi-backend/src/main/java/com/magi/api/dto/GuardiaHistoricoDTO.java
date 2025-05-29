package com.magi.api.dto;

import java.time.LocalDate;

public class GuardiaHistoricoDTO {

    private Long id;
    private String dniAsignat;
    private String dniAbsent;
    private String grupo;
    private String aula;
    private LocalDate fechaGuardia;

    private Integer sessionId;
    private String  hora;        

    public GuardiaHistoricoDTO() {
    }

    public GuardiaHistoricoDTO(Long id, String dniAsignat, String dniAbsent,
                               String grupo, String aula, LocalDate fechaGuardia,
                               Integer sessionId,
                               String hora) {
        this.id = id;
        this.dniAsignat = dniAsignat;
        this.dniAbsent = dniAbsent;
        this.grupo = grupo;
        this.aula = aula;
        this.fechaGuardia = fechaGuardia;
        this.sessionId     = sessionId;
        this.hora          = hora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDniAsignat() {
        return dniAsignat;
    }

    public void setDniAsignat(String dniAsignat) {
        this.dniAsignat = dniAsignat;
    }

    public String getDniAbsent() {
        return dniAbsent;
    }

    public void setDniAbsent(String dniAbsent) {
        this.dniAbsent = dniAbsent;
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

    public LocalDate getFechaGuardia() {
        return fechaGuardia;
    }

    public void setFechaGuardia(LocalDate fechaGuardia) {
        this.fechaGuardia = fechaGuardia;
    }
    
    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
