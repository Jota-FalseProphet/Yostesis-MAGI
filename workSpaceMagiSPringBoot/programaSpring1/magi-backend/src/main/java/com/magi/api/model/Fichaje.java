package com.magi.api.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name = "fichajes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "fecha"}))
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

    

    protected Fichaje() {}               // JPA
    public Fichaje(Usuario u, LocalDate f) {
        this.usuario = u;
        this.fecha   = f;
    }


    public Long getId(){ 
    	return id;
    }
    public Usuario getUsuario(){ 
    	return usuario; 
    }
    public LocalDate  getFecha(){ 
    	return fecha;
    }
    public LocalTime  getHoraInicio(){ 
    	return horaInicio; 
    }
    public LocalTime  getHoraFin(){ 
    	return horaFin; 
    }
    
    public void setHoraInicio(LocalTime h){ 
    	this.horaInicio = h; 
    }
    public void setHoraFin(LocalTime h){ 
    	this.horaFin = h; 
    }
}