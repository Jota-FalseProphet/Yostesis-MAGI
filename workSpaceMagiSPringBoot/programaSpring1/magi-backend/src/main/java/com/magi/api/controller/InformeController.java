package com.magi.api.controller;

import com.magi.api.model.FaltaDetalle;
import com.magi.api.service.FiltroInforme.Formato;
import com.magi.api.service.InformeService;
import com.magi.api.util.PeriodoUtils.Tipo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    @GetMapping("/faltas")
    public ResponseEntity<?> obtenerInforme(
            @RequestParam(required = false) Tipo periodo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ref,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,

            @RequestParam(required = false) Integer idDocente,
            @RequestParam(required = false) Integer idGrupo,

            @RequestParam(defaultValue = "JSON") Formato formato
    ) throws Exception {

        // JSON
        if (formato == Formato.JSON) {
            // dinámico por periodo
            if (periodo != null && ref != null) {
                var rango = com.magi.api.util.PeriodoUtils.calcular(periodo, ref);
                return ResponseEntity.ok(
                    informeService.obtenerDetalle(
                        rango.get("desde"),
                        rango.get("hasta"),
                        idDocente,
                        idGrupo
                    )
                );
            }
            // manual (desde / hasta)
            return ResponseEntity.ok(
                informeService.obtenerDetalle(desde, hasta, idDocente, idGrupo)
            );
        }

        // PDF
        if (formato == Formato.PDF) {
            ByteArrayResource file;
            if (periodo != null && ref != null) {
                file = informeService.generarPorPeriodo(periodo, ref, idDocente, idGrupo, formato);
            } else {
                // rango manual
                com.magi.api.service.FiltroInforme filtro = new com.magi.api.service.FiltroInforme();
                filtro.setDesde(desde);
                filtro.setHasta(hasta);
                filtro.setIdDocente(idDocente);
                filtro.setIdGrupo(idGrupo);
                filtro.setFormato(formato);
                file = informeService.generarInforme(filtro);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.attachment()
                    .filename("faltas_" + LocalDate.now() + ".pdf")
                    .build()
            );

            return new ResponseEntity<>(file, headers, HttpStatus.OK);
        }

        // Cualquier otro formato (por ejemplo XLSX) no está soportado
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Formato no soportado: solo se admiten JSON y PDF.");
    }
}
