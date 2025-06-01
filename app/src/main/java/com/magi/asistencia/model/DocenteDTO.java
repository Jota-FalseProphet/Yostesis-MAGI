package com.magi.asistencia.model;

public class DocenteDTO {

    private int id;
    private String document;
    private String nom;
    private String cognom1;
    private String cognom2;
    private String data_naix;

    public DocenteDTO() { }

    public int getId() {
        return id;
    }

    public String getDocument() {
        return document;
    }

    public String getNom() {
        return nom;
    }

    public String getCognom1() {
        return cognom1;
    }

    public String getCognom2() {
        return cognom2;
    }

    public String getData_naix() {
        return data_naix;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCognom1(String cognom1) {
        this.cognom1 = cognom1;
    }

    public void setCognom2(String cognom2) {
        this.cognom2 = cognom2;
    }

    public void setData_naix(String data_naix) {
        this.data_naix = data_naix;
    }


    public String getNombreCompleto() {
        return nom + " " + cognom1 + " " + cognom2;
    }

    @Override
    public String toString() {
        return nom;
    }
}
