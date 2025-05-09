package com.magi.api.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Fichaje iniciar(String string) {
        Usuario u   = usuarios.findById(string).orElseThrow();
        LocalDate h = LocalDate.now();

        Fichaje f = fichajes.findByUsuarioAndFecha(u, h)
                            .orElse(new Fichaje(u, h));

        if (f.getHoraInicio() != null)
            throw new IllegalStateException("Ya has fichado la entrada hoy");

        f.setHoraInicio(LocalTime.now());
        return fichajes.save(f);
    }

    @Transactional
    public Fichaje finalizar(String string) {
        Usuario u = usuarios.findById(string).orElseThrow();

        Fichaje f = fichajes.findByUsuarioAndFecha(u, LocalDate.now())
                            .orElseThrow(() -> new IllegalStateException("No has fichado entrada"));

        if (f.getHoraFin() != null)
            throw new IllegalStateException("Ya habías fichado la salida");

        f.setHoraFin(LocalTime.now());
        return fichajes.save(f);
    }
}