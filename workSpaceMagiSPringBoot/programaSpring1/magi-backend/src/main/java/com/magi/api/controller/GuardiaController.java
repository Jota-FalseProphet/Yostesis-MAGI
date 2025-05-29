package com.magi.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.magi.api.dto.GuardiaHistoricoDTO;
import com.magi.api.dto.SessionGuardiaDTO;
import com.magi.api.service.GuardiaService;

@RestController
@RequestMapping("/api/guardias")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GuardiaController {

    private final GuardiaService service;

    public GuardiaController(GuardiaService service) {
        this.service = service;
    }

    @GetMapping("/ausencias/vigentes")
    public List<SessionGuardiaDTO> ausenciasVigentes() {
        return service.listarAusenciasVigentes(LocalDate.now());
    }

    @GetMapping("/ausencias/dia")
    public List<SessionGuardiaDTO> ausenciasDelDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = (fecha != null ? fecha : LocalDate.now());
        return service.listarAusenciasDelDia(dia);
    }

    @PostMapping("/asignar")
    public ResponseEntity<Void> crear(
            @RequestParam("dniAsignat") String dniAsignat,
            @RequestParam("idSessio")  Long   idSessio) {
        service.asignarGuardia(dniAsignat.trim(), idSessio);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cubrir")
    public ResponseEntity<Void> cubrir(
            @RequestParam("dniAsignat") String dniAsignat,
            @RequestParam("idSessio")  Long   idSessio) {
        service.asignarGuardia(dniAsignat.trim(), idSessio);
        return ResponseEntity.ok().build();
    }

    /**
     * Histórico de guardias:
     * - Si se pasa ?dni=XXX (y admin=false), devuelve sólo las guardias de ese DNI.
     * - Si se pasa ?admin=true, devuelve todas las guardias de todos los docentes,
     *   agrupadas en un Map<dni, List<DTO>>.
     */
    @GetMapping("/historico")
    public ResponseEntity<?> historico(
            @RequestParam(value = "dni",   required = false) String dni,
            @RequestParam(value = "admin", required = false, defaultValue = "false") boolean admin) {

        if (admin) {
            // ADMIN: devolver todas agrupadas por DNI
            Map<String, List<GuardiaHistoricoDTO>> todas =
                service.historicoAgrupadoPorDocente();
            return ResponseEntity.ok(todas);
        }

        // Usuario normal: necesita parámetro 'dni'
        if (dni != null && !dni.isBlank()) {
            List<GuardiaHistoricoDTO> lista =
                service.historicoGuardiasPorDni(dni.trim());
            return ResponseEntity.ok(lista);
        }

        // Ni admin ni DNI ⇒ Bad Request
        return ResponseEntity
                .badRequest()
                .body("Parámetro 'dni' obligatorio para usuarios no administradores.");
    }
}