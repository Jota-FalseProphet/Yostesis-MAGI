package com.magi.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.magi.api.repository.UsuarioRepository;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioRepository repo;

    public AuthController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String,String> body) {

        String dni  = body.get("dni");
        String pass = body.get("password");

        return repo.findById(dni)                         
                   .filter(u -> pass.equals(u.getPassword()))
                   .map(u -> ResponseEntity.ok(u.getRol()))
                   .orElseGet(() ->
                       ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
