package com.magi.api.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.magi.api.config.AusenciasProperties;

/**
 * Job programado para generar ausencias automáticas de docentes que no han fichado
 * dentro de un margen de gracia configurable.
 */
@Component
public class AutogeneraAusenciasJob {

    private static final Logger LOG =
            LoggerFactory.getLogger(AutogeneraAusenciasJob.class);

    private final JdbcTemplate jdbc;
    private final AusenciasProperties props;
    private final String centreId;

    public AutogeneraAusenciasJob(
            JdbcTemplate jdbc,
            AusenciasProperties props,
            @Value("${magi.centre-id}") Long centreId
    ) {
        this.jdbc     = jdbc;
        this.props    = props;
        this.centreId = centreId.toString();
    }

    /**
     * Ejecuta cada 5 minutos (cron) en zona Europe/Madrid.
     * Llama al procedimiento almacenado autogenera_ausencias(interval, text).
     */
    @Scheduled(cron = "0 */5 * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutar() {
        Integer n = jdbc.queryForObject(
            "SELECT public.autogenera_ausencias(?::interval, ?::text)",
            Integer.class,
            props.getGraciaMin() + " min",
            centreId
        );

        LOG.info("autogenera_ausencias(): filas afectadas={}", n);
    }
}
