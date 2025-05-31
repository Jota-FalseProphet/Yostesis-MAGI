package com.magi.api.service;

import com.magi.api.dto.DocenteDTO;
import com.magi.api.model.Docent;
import com.magi.api.repository.DocentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para convertir entidades Docent a nuestro DTO DocenteDTO.
 */
@Service
public class DocenteService {

    private final DocentRepository docentRepository;

    public DocenteService(DocentRepository docentRepository) {
        this.docentRepository = docentRepository;
    }

    /**
     * Devuelve la lista completa de docentes, mapeada a DocenteDTO (id + nombre completo).
     */
    public List<DocenteDTO> listarTodos() {
        List<Docent> entidades = docentRepository.findAll();
        return entidades.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Docent a un DocenteDTO.
     * Concatenamos: nom + " " + cognom1 + " " + cognom2 (omitiendo nulos/vacíos).
     */
    private DocenteDTO convertirADTO(Docent entidad) {
        StringBuilder sb = new StringBuilder();

        if (entidad.getNom() != null && !entidad.getNom().isBlank()) {
            sb.append(entidad.getNom().trim());
        }
        if (entidad.getCognom1() != null && !entidad.getCognom1().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(entidad.getCognom1().trim());
        }
        if (entidad.getCognom2() != null && !entidad.getCognom2().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(entidad.getCognom2().trim());
        }

        String nombreCompleto = sb.toString();
        if (nombreCompleto.isBlank()) {
            // Si por alguna razón no hay nombre/cognoms, ponemos DNI o cadena vacía
            nombreCompleto = entidad.getDni() != null ? entidad.getDni() : "";
        }

        return new DocenteDTO(entidad.getIdDocent(), nombreCompleto);
    }
}
