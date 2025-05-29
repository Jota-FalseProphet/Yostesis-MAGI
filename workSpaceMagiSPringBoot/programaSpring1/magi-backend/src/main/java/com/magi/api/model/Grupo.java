package com.magi.api.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "grupo")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Integer idGrupo;

    // Mapeamos a la columna 'nombre' de la tabla
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nomGrupo;

    @Column(length = 20)
    private String etapa;

    @Column(length = 10)
    private String curso;

    protected Grupo() {}

    public Grupo(String nomGrupo, String etapa, String curso) {
        this.nomGrupo = nomGrupo;
        this.etapa    = etapa;
        this.curso    = curso;
    }

    public Integer getIdGrupo()   { return idGrupo; }
    public String  getNomGrupo()  { return nomGrupo; }
    public String  getEtapa()     { return etapa; }
    public String  getCurso()     { return curso; }

    public void setNomGrupo(String nomGrupo) { this.nomGrupo = nomGrupo; }
    public void setEtapa(String etapa)       { this.etapa    = etapa; }
    public void setCurso(String curso)       { this.curso    = curso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Grupo g)) return false;
        return Objects.equals(idGrupo, g.idGrupo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGrupo);
    }

    @Override
    public String toString() {
        return nomGrupo != null ? nomGrupo : "Grupo#" + idGrupo;
    }
}
