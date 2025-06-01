package com.magi.api.controller;

import com.magi.api.dto.GrupoDTO;
import com.magi.api.model.Grupo;
import com.magi.api.repository.GrupoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    private final GrupoRepository grupoRepository;

    public GrupoController(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    @GetMapping
    public List<GrupoDTO> listarGrupos() {
        return grupoRepository.findAll().stream()
            .map(g -> new GrupoDTO(g.getIdGrupo(), g.getNomGrupo()))
            .collect(Collectors.toList());
    }
}
