package com.magi.asistencia.model;

public class SesionDTO {
    private int id;
    private String horaDesde;
    private String horaFins;
    private String grupo;

    public SesionDTO(int id, String horaDesde, String horaFins, String grupo) {
        this.id = id;
        this.horaDesde = horaDesde;
        this.horaFins = horaFins;
        this.grupo = grupo;
    }

    public int getId() {
        return id;
    }

    public String getHoraDesde() {
        return horaDesde;
    }

    public String getHoraFins() {
        return horaFins;
    }

    public String getGrupo() {
        return grupo;
    }

    @Override
    public String toString() {
        return horaDesde + "–" + horaFins + " (" + grupo + ")";
    }
}
