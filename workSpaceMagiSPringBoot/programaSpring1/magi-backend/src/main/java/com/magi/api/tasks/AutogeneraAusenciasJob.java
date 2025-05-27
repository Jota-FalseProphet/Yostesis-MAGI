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
     * Ejecuta autogenera_ausencias() cada minuto exacto (segundo 0),
     * en horario de Europa/Madrid.
     */
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutar() {
        Integer n = jdbc.queryForObject(
            "SELECT public.autogenera_ausencias()",
            Integer.class
        );
        LOG.info("autogenera_ausencias(): filas afectadas={}", n);
    }
}
