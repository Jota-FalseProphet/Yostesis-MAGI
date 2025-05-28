package com.magi.api.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AutogeneraAusenciasJob {

    private static final Logger LOG = LoggerFactory.getLogger(AutogeneraAusenciasJob.class);

    private final JdbcTemplate jdbc;

    public AutogeneraAusenciasJob(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Cada minuto en el segundo 0, en horario de Europa/Madrid,
     * enlaza ausencias a sesiones (creadas previamente).
     */
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutarVinculacion() {
        Integer vinculadas = jdbc.queryForObject(
            "SELECT public.autogenera_ausencias()", Integer.class
        );
        LOG.info("autogenera_ausencias(): filas vinculadas={}", vinculadas);
    }

    /**
     * Cada minuto en el segundo 10 (para espaciarlo un poco),
     * en horario de Europa/Madrid, registra primero las ausencias
     * que pasen el periodo de gracia.
     */
    @Scheduled(cron = "10 * * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutarRegistroYVinculacion() {
        Integer nuevas = jdbc.queryForObject(
            "SELECT public.autogenera_registro_ausencias()", Integer.class
        );
        LOG.info("autogenera_registro_ausencias(): filas creadas={}", nuevas);

        Integer vinculadas = jdbc.queryForObject(
            "SELECT public.autogenera_ausencias()", Integer.class
        );
        LOG.info("autogenera_ausencias(): filas vinculadas={}", vinculadas);
    }

}
