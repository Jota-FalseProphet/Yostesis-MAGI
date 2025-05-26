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
 * Job programado que ejecuta el procedimiento almacenado
 * <pre>public.autogenera_ausencias(margen :: interval, centre_id bigint)</pre>
 * cada cinco minutos. El margen de cortesía (en minutos) y el identificador
 * del centro se leen de <code>application.yml</code> → <code>magi.*</code>.
 * <p>
 * Zona horaria explícita: <b>Europe/Madrid</b>.
 */
@Component
public class AutogeneraAusenciasJob {

    private static final Logger LOG = LoggerFactory.getLogger(AutogeneraAusenciasJob.class);

    private final JdbcTemplate jdbc;
    private final AusenciasProperties props;
    private final Long centreId;

    public AutogeneraAusenciasJob(JdbcTemplate jdbc,
                                  AusenciasProperties props,
                                  @Value("${magi.centre-id}") Long centreId) {
        this.jdbc     = jdbc;
        this.props    = props;
        this.centreId = centreId;
    }

    /**
     * Ejecuta <code>public.autogenera_ausencias(...)</code> cada 5&nbsp;minutos.
     * Envía el margen de cortesía ("<i>N</i> min") y el <code>centreId</code>.
     * Devuelve cuántas filas se generaron en <code>ausencies</code>.
     */
    @Scheduled(cron = "0 */5 * * * *", zone = "Europe/Madrid")
    @Transactional
    public void ejecutar() {
        String margen = props.getGraciaMin() + " min";
        Integer filas = jdbc.queryForObject(
                "SELECT public.autogenera_ausencias(?::interval, ?)",
                Integer.class,
                margen,
                centreId);

        LOG.info("autogenera_ausencias(): margen={} centreId={} filas={} ", margen, centreId, filas);
    }
}
