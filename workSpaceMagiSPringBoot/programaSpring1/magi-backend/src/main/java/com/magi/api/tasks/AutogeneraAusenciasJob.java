package com.magi.api.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.magi.api.config.AusenciasProperties;

@Component
public class AutogeneraAusenciasJob {

    private static final Logger LOG =
        LoggerFactory.getLogger(AutogeneraAusenciasJob.class);

    private final JdbcTemplate jdbc;
    private final AusenciasProperties props;

    public AutogeneraAusenciasJob(
            JdbcTemplate jdbc,
            AusenciasProperties props
    ) {
        this.jdbc  = jdbc;
        this.props = props;
    }

    /**
     * Ejecuta autogenera_ausencias() cada minuto exacto (segundo 0),
     * en horario de Europa/Madrid.
     */
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutar() {
        Integer n = jdbc.queryForObject(
            "SELECT public.autogenera_ausencias(?::interval, ?::text)",
            Integer.class,
            props.getGraciaMin() + " min",
            props.getPlantilla()
        );
        LOG.info("autogenera_ausencias(): filas afectadas={}", n);
    }
}
