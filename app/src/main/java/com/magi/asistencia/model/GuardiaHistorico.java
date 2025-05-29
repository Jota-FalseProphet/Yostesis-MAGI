package com.magi.asistencia.model;

import org.json.JSONException;
import org.json.JSONObject;

public class GuardiaHistorico {
    private long   id;
    private String dniAsignat;
    private String dniAbsent;
    private String grupo;
    private String aula;
    private String fechaGuardia;
    // NUEVOS
    private long   sessionId;
    private String hora;

    public GuardiaHistorico(long id, String dniAsignat, String dniAbsent,
                            String grupo, String aula, String fechaGuardia,
                            long sessionId, String hora) {
        this.id            = id;
        this.dniAsignat    = dniAsignat;
        this.dniAbsent     = dniAbsent;
        this.grupo         = grupo;
        this.aula          = aula;
        this.fechaGuardia  = fechaGuardia;
        this.sessionId     = sessionId;
        this.hora          = hora;
    }

    // getters
    public long   getId()           { return id; }
    public String getDniAsignat()   { return dniAsignat; }
    public String getDniAbsent()    { return dniAbsent; }
    public String getGrupo()        { return grupo; }
    public String getAula()         { return aula; }
    public String getFechaGuardia() { return fechaGuardia; }
    public long   getSessionId()    { return sessionId; }
    public String getHora()         { return hora; }

    /**
     * Construye un GuardiaHistorico a partir de un JSONObject y,
     * opcionalmente, forzando un dniAbsent si viene pasado en el segundo parámetro.
     */
    public static GuardiaHistorico fromJson(JSONObject o, String absentDni) throws JSONException {
        long   id         = o.getLong("id");
        String dniAsign   = o.optString("dniAsignat", "—");
        String dniAbs     = absentDni != null
                ? absentDni
                : o.optString("dniAbsent", "—");
        String grupo      = o.optString("grupo", "—");
        String aula       = o.optString("aula", "—");
        String fecha      = o.optString("fechaGuardia", "—");
        // LEEMOS LOS DOS CAMPOS NUEVOS
        long   sessId     = o.optLong("sessionId", -1);
        String hora       = o.optString("hora", "—");
        return new GuardiaHistorico(
                id, dniAsign, dniAbs, grupo, aula, fecha,
                sessId, hora
        );
    }
}
