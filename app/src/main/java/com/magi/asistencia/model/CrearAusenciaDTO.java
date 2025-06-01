package com.magi.asistencia.model;

import java.util.List;

public class CrearAusenciaDTO {
    private int idDocente;
    private String fecha;      // "YYYY-MM-DD"
    private boolean fullDay;
    private List<Integer> sesiones;
    private String motivo;

    public CrearAusenciaDTO() { }

    public CrearAusenciaDTO(int idDocente, String fecha, boolean fullDay, List<Integer> sesiones, String motivo) {
        this.idDocente = idDocente;
        this.fecha = fecha;
        this.fullDay = fullDay;
        this.sesiones = sesiones;
        this.motivo = motivo;
    }

    public int getIdDocente() {
        return idDocente;
    }
    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public boolean isFullDay() {
        return fullDay;
    }
    public void setFullDay(boolean fullDay) {
        this.fullDay = fullDay;
    }

    public List<Integer> getSesiones() {
        return sesiones;
    }
    public void setSesiones(List<Integer> sesiones) {
        this.sesiones = sesiones;
    }

    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
