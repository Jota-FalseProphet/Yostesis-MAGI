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

    //cada minuto en el segundo 0 del horario de madrid se enlazan las ausencias con las sesiones ya creada
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutarVinculacion() {
        Integer vinculadas = jdbc.queryForObject(
            "SELECT public.autogenera_ausencias()", Integer.class
        );
        LOG.info("autogenera_ausencias(): filas vinculadas={}", vinculadas);
    }


    //Aqui, cada minuto en el segundo 10 usando el horario de madrid registra todas las ausencias que sobrepasen el periodo de gracia de 5m
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
