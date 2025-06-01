// src/main/java/com/magi/asistencia/model/AusenciaIdDTO.java
package com.magi.asistencia.model;

public class AusenciaIdDTO {
    private int idAusencia;

    public AusenciaIdDTO() { }

    public AusenciaIdDTO(int idAusencia) {
        this.idAusencia = idAusencia;
    }

    public int getIdAusencia() {
        return idAusencia;
    }
    public void setIdAusencia(int idAusencia) {
        this.idAusencia = idAusencia;
    }
}
