package com.magi.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(length = 12)
    private String dni;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 72)
    private String password;

    @Column(nullable = false, length = 20)
    private String rol;

    public Usuario() { }                      

    public Usuario(String dni, String nombre,
                   String password, String rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }

    public String getDni()       { return dni; }
    public void   setDni(String d){ this.dni = d; }

    public String getNombre()    { return nombre; }
    public void   setNombre(String n){ this.nombre = n; }

    public String getPassword()  { return password; }
    public void   setPassword(String p){ this.password = p; }

    public String getRol()       { return rol; }
    public void   setRol(String r){ this.rol = r; }
}
