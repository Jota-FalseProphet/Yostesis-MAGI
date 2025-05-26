package com.magi.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "magi.ausencias")
public class AusenciasProperties {

    /** Margen de cortesía en minutos (e.g. 5) */
    private int graciaMin;

    public int getGraciaMin()        { return graciaMin; }
    public void setGraciaMin(int g)  { this.graciaMin = g; }
}
