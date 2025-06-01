package com.magi.api.migration;

import com.magi.api.model.Usuario;
import com.magi.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//aqui migro todos los psswd de texto plano a Bcrypt al arrancar el backend con el comando migrate
@Component
@Profile("migrate")
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UsuarioRepository usuarioRepository,
                                   BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info(">>> Iniciando migración de contraseñas en texto plano a BCrypt...");

        List<Usuario> todos = usuarioRepository.findAll();
        int migrados = 0;

        for (Usuario u : todos) {
            String pwd = u.getPassword();
            if (pwd == null || pwd.isBlank()) {
                log.warn("Usuario {} tiene password vacío, se omite.", u.getDni());
                continue;
            }

            boolean esBCrypt = pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$");
            if (esBCrypt) {
                continue;
            }

            String nuevoHash = passwordEncoder.encode(pwd);
            u.setPassword(nuevoHash);
            usuarioRepository.save(u);
            migrados++;
            log.info("Migrado usuario {}: password en texto plano → BCrypt", u.getDni());
        }

        log.info(">>> Migración completada. Total usuarios migrados: {}", migrados);
    }
}
