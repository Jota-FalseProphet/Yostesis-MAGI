package com.magi.api.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.magi.api.dto.CrearAusenciaDTO;
import com.magi.api.model.Ausencia;
import com.magi.api.model.AusenciaSessio;
import com.magi.api.model.AusenciaSessioId;
import com.magi.api.model.SessionHorario;
import com.magi.api.model.Docent;
import com.magi.api.repository.AusenciaRepository;
import com.magi.api.repository.AusenciaSessioRepository;
import com.magi.api.repository.SessionHorarioRepository;
import com.magi.api.repository.DocentRepository;

@Service
public class AusenciaService {

    private final AusenciaRepository       repoAus;
    private final AusenciaSessioRepository repoLink;
    private final SessionHorarioRepository repoSes;
    private final DocentRepository         repoDoc;

    public AusenciaService(
            AusenciaRepository repoAus,
            AusenciaSessioRepository repoLink,
            SessionHorarioRepository repoSes,
            DocentRepository repoDoc) {

        this.repoAus  = repoAus;
        this.repoLink = repoLink;
        this.repoSes  = repoSes;
        this.repoDoc  = repoDoc;
    }

    @Transactional
    public int crearAusencia(CrearAusenciaDTO dto) {
        //meter ausencias para el admin
        if (repoAus.existsByDocent_IdDocentAndFechaAusencia(dto.getIdDocente(), dto.getFecha())) {
            throw new IllegalStateException("YA_EXISTE_FALTA");
        }

       
        if (!dto.isFullDay()) {
            LocalDate hoy = LocalDate.now(ZoneId.of("Europe/Madrid"));
            if (dto.getFecha().isEqual(hoy)) {
                List<SessionHorario> sesionesHoy = repoSes.findAllById(dto.getSesiones());
                LocalTime finUltima = sesionesHoy.stream()
                        .map(SessionHorario::getHoraFins)
                        .max(LocalTime::compareTo)
                        .orElse(LocalTime.MIN);

                LocalTime ahora = LocalTime.now(ZoneId.of("Europe/Madrid"));
                if (ahora.isAfter(finUltima)) {
                    throw new IllegalStateException("SESION_FINISHED");
                }
            }
        }

       
        Ausencia ent = new Ausencia();
        Docent docente = repoDoc.findById(dto.getIdDocente())
                .orElseThrow(() -> new IllegalStateException("DOCENTE_NO_ENCONTRADO: " + dto.getIdDocente()));
        ent.setDocent(docente);
        ent.setFechaAusencia(dto.getFecha());
        ent.setFullDay(dto.isFullDay());
        ent.setMotivo(dto.getMotivo());
        repoAus.save(ent);

        if (!dto.isFullDay()) {
            for (Integer idSes : dto.getSesiones()) {
                SessionHorario sesion = repoSes.findById(idSes)
                        .orElseThrow(() -> new IllegalStateException("SESSION_NO_ENCONTRADA: " + idSes));

                AusenciaSessioId puenteId = new AusenciaSessioId(ent.getIdAusencia(), sesion.getIdSessio());
                AusenciaSessio link = new AusenciaSessio();
                link.setId(puenteId);
                link.setAusencia(ent);
                link.setSession(sesion);
                repoLink.save(link);
            }
        }

        return ent.getIdAusencia();
    }
}
