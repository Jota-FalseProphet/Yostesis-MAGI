// src/main/java/com/magi/api/model/SessionHorario.java
package com.magi.api.model;

import java.time.LocalTime;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "sessions_horari")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SessionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessio")
    private Integer idSessio;

    // mapea la columna plantilla (tu "grupo" en DTO)
    @Column(name = "plantilla", nullable = false)
    private String plantilla;

    @Column(name = "dia_setmana", nullable = false)
    private String diaSetmana;

    @Column(name = "hora_desde", nullable = false)
    private LocalTime horaDesde;

    @Column(name = "hora_fins", nullable = false)
    private LocalTime horaFins;

    // relación ManyToOne con Aula
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aula")
    private Aula aula;

    public SessionHorario() {}

    // getters y setters para todas las propiedades

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

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }
}
