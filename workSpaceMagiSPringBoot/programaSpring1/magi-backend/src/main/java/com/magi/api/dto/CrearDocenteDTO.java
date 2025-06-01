package com.magi.api.dto;

public class CrearDocenteDTO {

    private String dni;
    private String nombreCompleto;
    private String contrasena;

    public CrearDocenteDTO() { }

    public CrearDocenteDTO(String dni, String nombreCompleto, String contrasena) {
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.contrasena = contrasena;
    }

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
