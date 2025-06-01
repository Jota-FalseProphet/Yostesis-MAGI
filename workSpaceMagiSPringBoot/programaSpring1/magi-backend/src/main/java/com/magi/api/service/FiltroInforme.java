package com.magi.api.service;

import java.time.LocalDate;

public class FiltroInforme {
	
	
	//llamo entidades para armarme mis filtros
    private LocalDate desde;          
    private LocalDate hasta;          
    private Integer   idDocente;      
    private Integer   idGrupo;        
    private Formato   formato = Formato.JSON;

    public enum Formato { JSON, PDF, XLSX }

	public LocalDate getDesde() {
		return desde;
	}

	public void setDesde(LocalDate desde) {
		this.desde = desde;
	}

	public LocalDate getHasta() {
		return hasta;
	}

	public void setHasta(LocalDate hasta) {
		this.hasta = hasta;
	}

	public Integer getIdDocente() {
		return idDocente;
	}

	public void setIdDocente(Integer idDocente) {
		this.idDocente = idDocente;
	}

	public Integer getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(Integer idGrupo) {
		this.idGrupo = idGrupo;
	}

	public Formato getFormato() {
		return formato;
	}

	public void setFormato(Formato formato) {
		this.formato = formato;
	}


}
