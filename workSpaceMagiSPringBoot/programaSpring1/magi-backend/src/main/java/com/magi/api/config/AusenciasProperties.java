package com.magi.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "magi.ausencias")
public class AusenciasProperties {

    /**
     * Minutos de gracia antes de generar ausencia.
     */
    private int graciaMin;

    /**
     * Código de plantilla para filtrar sesiones.
     */
    private String plantilla;

    public int getGraciaMin() {
        return graciaMin;
    }

    public void setGraciaMin(int graciaMin) {
        this.graciaMin = graciaMin;
    }

    public String getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(String plantilla) {
        this.plantilla = plantilla;
    }
}
