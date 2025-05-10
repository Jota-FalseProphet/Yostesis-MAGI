package com.magi.api.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;

import jakarta.persistence.*;

@Entity
@Table(name = "fichajes")
public class Fichaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    //ESTO ES PARA QUE EL TOTAL DE HORAS CALCE CON EL CAMPO TOTAL EN LA TABLA PSQL
    @Type(PostgreSQLIntervalType.class)
    @Column(name = "total", columnDefinition = "interval")
    private Duration total;

    protected Fichaje() {} // JPA

    public Fichaje(Usuario u, LocalDate f) {
        this.usuario = u;
        this.fecha   = f;
        this.total   = Duration.ZERO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public Duration getTotal() {
        return total;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public void setTotal(Duration total) {
        this.total = total;
    }
    //ES QUE AQUI PEDIA MINUTOS ENTEROS Y QUEDABA RARO
    @JsonProperty("totalFormateado")
    public String getTotalFormateado() {
        long s = total.getSeconds();
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long sec = s % 60;
        return String.format("%02d:%02d:%02d", h, m, sec);
    }

}
