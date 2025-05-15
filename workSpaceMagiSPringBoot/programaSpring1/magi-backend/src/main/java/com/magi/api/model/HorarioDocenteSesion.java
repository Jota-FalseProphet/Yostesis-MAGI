package com.magi.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalTime;

@Entity
@Immutable
@Table(name = "vw_horario_docente_sesion")
public class HorarioDocenteSesion {

    @Id
    @Column(name = "id_sesion")
    private Integer idSesion;

    private String plantilla;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "orden_sesion")
    private Integer ordenSesion;

    @Column(name = "hora_desde")
    private LocalTime horaDesde;

    @Column(name = "hora_hasta")
    private LocalTime horaHasta;

    @Column(name = "id_docente")
    private Integer idDocente;

    @Column(name = "nombre_docente")
    private String nombreDocente;

    private String ocupacion;

    /* getters — sin setters (vista de solo lectura supongo que lo usaré para el modulo de informes) */
    public Integer getIdSesion()       { return idSesion; }
    public String  getPlantilla()      { return plantilla; }
    public String  getDiaSemana()      { return diaSemana; }
    public Integer getOrdenSesion()    { return ordenSesion; }
    public LocalTime getHoraDesde()    { return horaDesde; }
    public LocalTime getHoraHasta()    { return horaHasta; }
    public Integer getIdDocente()      { return idDocente; }
    public String  getNombreDocente()  { return nombreDocente; }
    public String  getOcupacion()      { return ocupacion; }
}
