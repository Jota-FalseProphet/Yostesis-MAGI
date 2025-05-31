package com.magi.api.dto;

/**
 * DTO simplificado de un docente: solo id y nombre completo.
 * La app Android espera dos propiedades: "id" (Integer) y "nombre" (String).
 */
public class DocenteDTO {

    private Integer id;
    private String nombre;

    public DocenteDTO() {
    }

    public DocenteDTO(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
