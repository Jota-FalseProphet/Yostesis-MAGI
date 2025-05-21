// src/main/java/com/magi/api/model/Aula.java
package com.magi.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @Column(name = "id_aula")
    private Integer idAula;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    // getters y setters
    public Integer getIdAula() { return idAula; }
    public void setIdAula(Integer idAula) { this.idAula = idAula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
