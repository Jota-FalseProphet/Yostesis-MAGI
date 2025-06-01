package com.magi.asistencia.model;

public class Docente {
    private int id;
    private String nombre;

    public Docente(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
