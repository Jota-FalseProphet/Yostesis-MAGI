package com.magi.api.model;

import jakarta.persistence.Entity;   
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity                     
@Table(name = "usuarios")   
public class Usuario {

    @Id
    private String dni;

    private String nombre;
    private String password;
    private String rol;

   

    public String getDni(){ 
    	return dni; 
    }public void   setDni(String dni){ 
    	this.dni = dni; 
    }

    
    public String getNombre(){ 
    	return nombre;
    }public void   setNombre(String nombre){ 
    	this.nombre = nombre; 
    }

    
    public String getPassword(){ 
    	return password; 
    }public void   setPassword(String password){ 
    	this.password = password; 
    }

    public String getRol(){ 
    	return rol;
    }public void   setRol(String rol){ 
    	this.rol = rol; 
    }
}
