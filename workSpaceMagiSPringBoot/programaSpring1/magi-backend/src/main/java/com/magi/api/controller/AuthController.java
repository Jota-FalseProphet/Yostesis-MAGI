package com.magi.api.controller;

import com.magi.api.model.Usuario;
import com.magi.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioRepository repo;
    private static final PasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    public AuthController(UsuarioRepository repo) { this.repo = repo; }

    @PostMapping("/login")
    public String login(@RequestBody Credenciales c) {
        Usuario u = repo.findByDni(c.getDni())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String stored = u.getPassword();
        boolean ok;

        if (stored.startsWith("$2")) {                       // contraseña ya en BCrypt
            ok = BCRYPT.matches(c.getPassword(), stored);
        } else {                                            // texto plano
            ok = c.getPassword().equals(stored);
            if (ok) {                                       // se migra automático
                u.setPassword(BCRYPT.encode(c.getPassword()));
                repo.save(u);
            }
        }

        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return u.getRol();                                  // "PROFESOR", "ADMIN"…
    }

    /* DTO simple */
    public static class Credenciales {
        private String dni;
        private String password;
        public String getDni() { return dni; }
        public void setDni(String dni) { this.dni = dni; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
