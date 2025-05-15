// src/main/java/com/magi/api/model/SessionHorario.java
package com.magi.api.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "sessions_horari")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SessionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessio")
    private Integer idSessio;

    @Column(name = "plantilla")
    private String plantilla;

    @Column(name = "dia_setmana")
    private String diaSetmana;

    @Column(name = "sessio_ordre")
    private Integer sessioOrdre;

    @Column(name = "hora_desde")
    private LocalTime horaDesde;

    @Column(name = "hora_fins")
    private LocalTime horaFins;

    public SessionHorario() {}

    public Integer getIdSessio() {
        return idSessio;
    }
    public void setIdSessio(Integer idSessio) {
        this.idSessio = idSessio;
    }

    public String getPlantilla() {
        return plantilla;
    }
    public void setPlantilla(String plantilla) {
        this.plantilla = plantilla;
    }

    public String getDiaSetmana() {
        return diaSetmana;
    }
    public void setDiaSetmana(String diaSetmana) {
        this.diaSetmana = diaSetmana;
    }

    public Integer getSessioOrdre() {
        return sessioOrdre;
    }
    public void setSessioOrdre(Integer sessioOrdre) {
        this.sessioOrdre = sessioOrdre;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }
    public void setHoraDesde(LocalTime horaDesde) {
        this.horaDesde = horaDesde;
    }

    public LocalTime getHoraFins() {
        return horaFins;
    }
    public void setHoraFins(LocalTime horaFins) {
        this.horaFins = horaFins;
    }
}
