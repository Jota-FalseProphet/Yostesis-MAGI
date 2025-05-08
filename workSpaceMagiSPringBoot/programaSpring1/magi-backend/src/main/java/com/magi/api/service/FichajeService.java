package com.magi.api.service;

import com.magi.api.model.Fichaje;
import com.magi.api.model.Usuario;
import com.magi.api.repository.FichajeRepository;
import com.magi.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class FichajeService {

    private final FichajeRepository fichajeRepo;
    private final UsuarioRepository usuarioRepo;

    public FichajeService(FichajeRepository fichajeRepo,
                          UsuarioRepository usuarioRepo) {
        this.fichajeRepo = fichajeRepo;
        this.usuarioRepo  = usuarioRepo;
    }

    public void iniciar(String dni) {
        // conflicto si ya hay jornada abierta
        fichajeRepo.findFirstByUsuarioDniAndHoraFinIsNull(dni)
            .ifPresent(f -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya tienes jornada iniciada");
            });

        Usuario u = usuarioRepo.findByDni(dni)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Fichaje f = new Fichaje(u, LocalDate.now());
        f.setHoraInicio(LocalTime.now());
        fichajeRepo.save(f);
    }

    public void finalizar(String dni) {
        Fichaje f = fichajeRepo.findFirstByUsuarioDniAndHoraFinIsNull(dni)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT, "No tienes jornada abierta"));

        f.setHoraFin(LocalTime.now());
        fichajeRepo.save(f);
    }
}
