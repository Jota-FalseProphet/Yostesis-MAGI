package com.magi.asistencia.model;

public class Informe {
    private int id;
    private String docente;
    private String grupo;
    private String fecha;
    private int totalFaltas;

    public Informe() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocente() { return docente; }
    public void setDocente(String docente) { this.docente = docente; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getTotalFaltas() { return totalFaltas; }
    public void setTotalFaltas(int totalFaltas) { this.totalFaltas = totalFaltas; }
}
