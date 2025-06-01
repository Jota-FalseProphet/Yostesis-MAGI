package com.magi.api.dto;

public class SesionDTO {
    private int id;
    private String horaDesde;
    private String horaFins;
    private String grupo;

    // Constructor vacío
    public SesionDTO() {
    }

    // Constructor con todos los campos
    public SesionDTO(int id, String horaDesde, String horaFins, String grupo) {
        this.id = id;
        this.horaDesde = horaDesde;
        this.horaFins = horaFins;
        this.grupo = grupo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHoraDesde() {
        return horaDesde;
    }

    public void setHoraDesde(String horaDesde) {
        this.horaDesde = horaDesde;
    }

    public String getHoraFins() {
        return horaFins;
    }

    public void setHoraFins(String horaFins) {
        this.horaFins = horaFins;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
