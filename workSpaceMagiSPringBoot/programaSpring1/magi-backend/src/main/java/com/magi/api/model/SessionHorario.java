// src/main/java/com/magi/api/model/SessionHorario.java
package com.magi.api.model;

import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "sessions_horari")  // según :contentReference[oaicite:0]{index=0}
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class SessionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessio")
    private Integer idSessio;

    // Si quieres mapear también Grupo, crea la entidad Grupo y sustituye:
    // @Column(name="plantilla") private String plantilla;
    // por:
    // @ManyToOne @JoinColumn(name="id_grupo") private Grupo grupo;

    @Column(name = "dia_setmana")
    private String diaSetmana;

    @Column(name = "sessio_ordre")
    private Integer sessioOrdre;

    @Column(name = "hora_desde")
    private LocalTime horaDesde;

    @Column(name = "hora_fins")
    private LocalTime horaFins;

    // —— Relación a Aula ——
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aula", nullable = false)
    private Aula aula;

    public SessionHorario() {}

    // getters y setters

    public Integer getIdSessio() { return idSessio; }
    public void setIdSessio(Integer idSessio) { this.idSessio = idSessio; }

    public String getDiaSetmana() { return diaSetmana; }
    public void setDiaSetmana(String diaSetmana) { this.diaSetmana = diaSetmana; }

    public Integer getSessioOrdre() { return sessioOrdre; }
    public void setSessioOrdre(Integer sessioOrdre) { this.sessioOrdre = sessioOrdre; }

    public LocalTime getHoraDesde() { return horaDesde; }
    public void setHoraDesde(LocalTime horaDesde) { this.horaDesde = horaDesde; }

    public LocalTime getHoraFins() { return horaFins; }
    public void setHoraFins(LocalTime horaFins) { this.horaFins = horaFins; }

    public Aula getAula() { return aula; }
    public void setAula(Aula aula) { this.aula = aula; }
}
