package com.magi.asistencia.model;

/** POJO que representa una franja sin cubrir / cubierta. */
public class SessionHorario {

    private long idSessio;
    private String grupo;
    private String aula;
    private String horaInicio;   // “09:55”
    private String horaFin;      // “10:50”
    private Boolean cubierta;    // true = ya asignada
    private String profesorGuardia; // DNI del profesor que la cubre (o null)

    public SessionHorario() {}   // requerido por Gson

    public SessionHorario(long id, String grupo, String aula,
                          String horaInicio, String horaFin,
                          Boolean cubierta, String profesorGuardia) {
        this.idSessio = id;
        this.grupo = grupo;
        this.aula = aula;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.cubierta = cubierta;
        this.profesorGuardia = profesorGuardia;
    }

    /* ——— Getters & setters ——— */
    public long getIdSessio() { return idSessio; }
    public String getGrupo() { return grupo; }
    public String getAula() { return aula; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
    public Boolean getCubierta() { return cubierta; }
    public String getProfesorGuardia() { return profesorGuardia; }

    public void setCubierta(Boolean cubierta) { this.cubierta = cubierta; }
}
