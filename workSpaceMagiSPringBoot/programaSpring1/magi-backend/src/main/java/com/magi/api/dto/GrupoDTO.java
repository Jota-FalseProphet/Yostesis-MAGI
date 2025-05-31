// src/main/java/com/magi/api/dto/GrupoDTO.java

package com.magi.api.dto;

public class GrupoDTO {
    private Integer idGrupo;
    private String nombre;

    public GrupoDTO(Integer idGrupo, String nombre) {
        this.idGrupo = idGrupo;
        this.nombre  = nombre;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public String getNombre() {
        return nombre;
    }
}
