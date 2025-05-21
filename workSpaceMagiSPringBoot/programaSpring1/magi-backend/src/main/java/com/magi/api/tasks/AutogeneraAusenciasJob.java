package com.magi.api.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AutogeneraAusenciasJob {

    private static final Logger LOG =
            LoggerFactory.getLogger(AutogeneraAusenciasJob.class);

    private final JdbcTemplate jdbc;

    public AutogeneraAusenciasJob(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Esto ejecuta un procedimiento almacenado que crea ausencias para los
     * docentes que no han fichado antes de un margen de cortesía dado por mí de 5 min.
     *
     *Se hace cada 5 minutos  
     *Europea/Madrid
     */
    @Scheduled(cron = "0 */5 * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutar() {
    	Integer n = jdbc.queryForObject(
    	        "SELECT public.autogenera_ausencias('5 min','366781135')",
    	        Integer.class);
    	LOG.info("autogenera_ausencias(): filas afectadas={}", n);

    }

}
