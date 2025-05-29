package com.magi.api.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "docent")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Docent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docent")
    private Integer idDocent;

    @Column(name = "document", nullable = false)
    private String dni;  

    @Column(name = "nom")
    private String nom;

    @Column(name = "cognom1")
    private String cognom1;

    @Column(name = "cognom2")
    private String cognom2;

    @Column(name = "tipus_doc")
    private String tipusDoc;

    @Column(name = "sexe")
    private String sexe;

    @Column(name = "data_ingres")
    private LocalDate dataIngres;

    

    public Docent() {}

    public Integer getIdDocent() {
        return idDocent;
    }

    public void setIdDocent(Integer idDocent) {
        this.idDocent = idDocent;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCognom1() {
        return cognom1;
    }

    public void setCognom1(String cognom1) {
        this.cognom1 = cognom1;
    }

    public String getCognom2() {
        return cognom2;
    }

    public void setCognom2(String cognom2) {
        this.cognom2 = cognom2;
    }

    public String getTipusDoc() {
        return tipusDoc;
    }

    public void setTipusDoc(String tipusDoc) {
        this.tipusDoc = tipusDoc;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public LocalDate getDataIngres() {
        return dataIngres;
    }

    public void setDataIngres(LocalDate dataIngres) {
        this.dataIngres = dataIngres;
    }

    
}
