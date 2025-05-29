package com.magi.asistencia.model;

public class GuardiaHistorico {
    private long id;
    private String dniAsignat;
    private String dniAbsent;
    private String grupo;
    private String aula;
    private String fechaGuardia;

    public GuardiaHistorico(long id, String dniAsignat, String dniAbsent,
                            String grupo, String aula, String fechaGuardia) {
        this.id            = id;
        this.dniAsignat    = dniAsignat;
        this.dniAbsent     = dniAbsent;
        this.grupo         = grupo;
        this.aula          = aula;
        this.fechaGuardia  = fechaGuardia;
    }

    public long   getId()           { return id; }
    public String getDniAsignat()   { return dniAsignat; }
    public String getDniAbsent()    { return dniAbsent; }
    public String getGrupo()        { return grupo; }
    public String getAula()         { return aula; }
    public String getFechaGuardia() { return fechaGuardia; }
}
