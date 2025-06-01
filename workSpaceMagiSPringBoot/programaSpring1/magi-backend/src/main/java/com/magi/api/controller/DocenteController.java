package com.magi.api.controller;

import com.magi.api.dto.CrearDocenteDTO;
import com.magi.api.dto.DocenteDTO;
import com.magi.api.service.DocenteService;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }


    @GetMapping
    public ResponseEntity<List<DocenteDTO>> listarDocentes() {
        List<DocenteDTO> lista = docenteService.listarTodos();
        return ResponseEntity.ok(lista);
    }


    @PostMapping
    public ResponseEntity<?> crearDocente(@RequestBody CrearDocenteDTO crearDto) {
        try {
            DocenteDTO dtoCreado = docenteService.crearDocente(crearDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(dtoCreado);

        } catch (DataIntegrityViolationException dive) {
            String msg = dive.getMessage();
            if (msg != null && msg.contains("Ya existe un usuario")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("{\"error\":\"" + msg + "\"}");
            }
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error en la base de datos al crear docente.\"}");

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Error interno del servidor.\"}");
        }
    }
    
    @GetMapping("/{dni}")
    public ResponseEntity<DocenteDTO> obtenerDocentePorDni(@PathVariable("dni") String dni) {
        DocenteDTO dto = docenteService.buscarPorDocumento(dni);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
