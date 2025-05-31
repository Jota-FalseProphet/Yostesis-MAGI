// Docente.java
package com.magi.asistencia.model;
public class Docente {
    private int id; private String nombre;
    public Docente(int id,String n){this.id=id;this.nombre=n;}
    public int getId(){return id;}
    @Override public String toString(){return nombre;}
}
