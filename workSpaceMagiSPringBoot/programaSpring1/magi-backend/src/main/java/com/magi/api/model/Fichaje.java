package com.magi.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fichaje")
public class Fichaje {

	@Id @GeneratedValue
	private Long id;
	
	private String dni;
	
	private LocalDateTime fechaHora;
	
	@Enumerated(EnumType.STRING)
	private Tipo tipo;      
	
	public enum Tipo { IN, OUT }
	
	public Fichaje() {} //PARA EL JPA
	public Fichaje(String dni, LocalDateTime fechaHora, Tipo tipo) {
		this.dni = dni;
		this.fechaHora = fechaHora;
		this.tipo = tipo;
	}
	
	 public Long getId(){ 
		 return id;
	 }
	 public String getDni(){ 
		 return dni; 
	 }
	 public LocalDateTime getFechaHora(){ 
		 return fechaHora; 
	 }
	 public Tipo getTipo(){ 
		 return tipo; 
	 }

	 public void setId(Long id){ 
		 this.id = id; 	 
	 }
	 public void setDni(String dni){ 
		 this.dni = dni; 
	 }
	 public void setFechaHora(LocalDateTime fechaHora){ 
		 this.fechaHora = fechaHora; 
	 }
	 public void setTipo(Tipo tipo){ 
		 this.tipo = tipo; 
	 }	

 
}
