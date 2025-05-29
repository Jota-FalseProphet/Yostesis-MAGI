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

    @Column(name = "plantilla", insertable = false, updatable = false)
    private String plantilla;  // READ‑ONLY

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_grupo", nullable = false)
    private Grupo grupo;


    @Column(name = "dia_setmana", nullable = false)
    private String diaSetmana;

    @Column(name = "hora_desde", nullable = false)
    private LocalTime horaDesde;

    @Column(name = "hora_fins", nullable = false)
    private LocalTime horaFins;
    
    @Column(name = "sessio_ordre", nullable = false)
    private Integer sessioOrdre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aula")
    private Aula aula;


    protected SessionHorario() {}

    public SessionHorario(Grupo grupo, String diaSetmana,
                          LocalTime horaDesde, LocalTime horaFins, Aula aula) {
        this.grupo      = grupo;
        this.diaSetmana = diaSetmana;
        this.horaDesde  = horaDesde;
        this.horaFins   = horaFins;
        this.aula       = aula;
    }

    public Integer getIdSessio()    { return idSessio;    }
    public String  getPlantilla()   { return plantilla;   } 
    public Grupo   getGrupo()       { return grupo;       }
    public String  getDiaSetmana()  { return diaSetmana;  }
    public LocalTime getHoraDesde() { return horaDesde;   }
    public LocalTime getHoraFins()  { return horaFins;    }
    public Aula    getAula()        { return aula;        }
    public Integer getSessioOrdre() {
        return sessioOrdre;
    }

    public void setGrupo(Grupo grupo)             { this.grupo = grupo; }
    public void setDiaSetmana(String diaSetmana)  { this.diaSetmana = diaSetmana; }
    public void setHoraDesde(LocalTime horaDesde) { this.horaDesde  = horaDesde; }
    public void setHoraFins(LocalTime horaFins)   { this.horaFins   = horaFins;  }
    public void setAula(Aula aula)                { this.aula = aula; }
    public void setSessioOrdre(Integer sessioOrdre) {
        this.sessioOrdre = sessioOrdre;
    }


 

    @Override
    public String toString() {
        return "Sessio " + idSessio + " - " + grupo.getNomGrupo() +
               " (" + horaDesde + "–" + horaFins + ")";
    }
}
