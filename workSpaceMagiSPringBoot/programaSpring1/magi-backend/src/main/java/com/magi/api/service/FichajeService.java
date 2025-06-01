package com.magi.api.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.magi.api.model.Fichaje;
import com.magi.api.model.Usuario;
import com.magi.api.repository.FichajeRepository;
import com.magi.api.repository.UsuarioRepository;

@Service
public class FichajeService {

    private final FichajeRepository fichajes;
    private final UsuarioRepository usuarios;

    public FichajeService(FichajeRepository fichajes, UsuarioRepository usuarios) {
        this.fichajes = fichajes;
        this.usuarios = usuarios;
    }

    //control de erroes para la hora de fichar y tales
    @Transactional
    public Fichaje iniciar(String dni) {
        Usuario u = usuarios.findById(dni)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            );

        LocalDate hoy = LocalDate.now();

       
        fichajes
            .findTopByUsuarioAndFechaAndHoraFinIsNullOrderByHoraInicioDesc(u, hoy)
            .ifPresent(f -> {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tienes un fichaje sin cerrar"
                );
            });

        
        Fichaje nuevo = new Fichaje(u, hoy);
        nuevo.setHoraInicio(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        nuevo.setTotal(Duration.ZERO);
        return fichajes.save(nuevo);
    }

    //lo mismo pero para la salida
    @Transactional
    public Fichaje finalizar(String dni) {
        Usuario u = usuarios.findById(dni)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
            );

        LocalDate hoy = LocalDate.now();

        // busco el tramo abierto mas reciente
        Fichaje abierto = fichajes
            .findTopByUsuarioAndFechaAndHoraFinIsNullOrderByHoraInicioDesc(u, hoy)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tienes ningún fichaje abierto")
            );

        // cierro ese tramo el truncate para comerme los milisegundos ydejarlo soloen segundos
        abierto.setHoraFin(LocalTime.now().truncatedTo(ChronoUnit.SECONDS));

        // recalculo TODOS los tramos
        List<Fichaje> tramosHoy = fichajes.findByUsuarioAndFecha(u, hoy);
        Duration total = tramosHoy.stream()
            .filter(f -> f.getHoraInicio() != null && f.getHoraFin() != null)
            .map(f -> Duration.between(
                f.getHoraInicio().truncatedTo(ChronoUnit.SECONDS),
                f.getHoraFin().truncatedTo(ChronoUnit.SECONDS)
            ))
            .reduce(Duration.ZERO, Duration::plus);

        abierto.setTotal(total);
        return fichajes.save(abierto);
    }
}
