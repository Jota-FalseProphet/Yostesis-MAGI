package com.magi.asistencia.model;

import org.json.JSONObject;


public class SessionHorario {

    private long idSessio;
    private String grupo;
    private String aula;
    private String horaInicio;
    private String horaFin;
    private Boolean cubierta;
    private String profesorGuardia;

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

    public long getIdSessio() { return idSessio; }
    public String getGrupo() { return grupo; }
    public String getAula() { return aula; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
    public Boolean getCubierta() { return cubierta; }
    public String getProfesorGuardia() { return profesorGuardia; }

    public void setIdSessio(long idSessio) { this.idSessio = idSessio; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public void setAula(String aula) { this.aula = aula; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
    public void setCubierta(Boolean cubierta) { this.cubierta = cubierta; }
    public void setProfesorGuardia(String profesorGuardia) { this.profesorGuardia = profesorGuardia; }


    public static SessionHorario fromJson(JSONObject o) {
        long id = o.optLong("id", -1);
        String grupo = o.optString("grupo", "—");
        String aula = o.optString("aula", "—");
        String horaInicio = o.optString("horaDesde", "");
        String horaFin = o.optString("horaHasta", "");
        Boolean cubierta = o.has("cubierta") ? o.optBoolean("cubierta") : null;
        JSONObject asignat = o.optJSONObject("docentAssignat");
        String profesor = null;
        if (asignat != null) {
            profesor = asignat.optString("dni", null);
            if (profesor == null) {
                String nom = asignat.optString("nom", "");
                String cog1 = asignat.optString("cognom1", "");
                profesor = (nom + " " + cog1).trim();
            }
        }
        return new SessionHorario(
                id, grupo, aula,
                horaInicio, horaFin,
                cubierta, profesor
        );
    }

}
