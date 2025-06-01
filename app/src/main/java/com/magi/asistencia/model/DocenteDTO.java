package com.magi.asistencia.model;

public class DocenteDTO {

    private int id;
    private String document;   // DNI
    private String nom;        // nombre
    private String cognom1;    // primer apellido
    private String cognom2;    // segundo apellido
    private String data_naix;  // fecha de nacimiento, formato "YYYY-MM-DD"

    // Constructor por defecto (necesario para que Gson/Retrofit pueda instanciarlo)
    public DocenteDTO() { }

    // Getters
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

    /**
     * Método auxiliar para devolver "nombre + apellidos" concatenados.
     */
    public String getNombreCompleto() {
        return nom + " " + cognom1 + " " + cognom2;
    }

    @Override
    public String toString() {
        // Si en algún Spinner o ArrayAdapter usas directamente toString(),
        // mostrará solo 'nom'.
        return nom;
    }
}
