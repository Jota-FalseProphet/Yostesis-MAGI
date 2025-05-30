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
            // dinámico
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
            // manual
            return ResponseEntity.ok(
                informeService.obtenerDetalle(desde, hasta, idDocente, idGrupo)
            );
        }

        // PDF / XLSX
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

        String contentType = formato == Formato.PDF
                ? MediaType.APPLICATION_PDF_VALUE
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String ext = formato == Formato.PDF ? ".pdf" : ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                    .filename("faltas_" + LocalDate.now() + ext)
                    .build()
        );

        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }
}
