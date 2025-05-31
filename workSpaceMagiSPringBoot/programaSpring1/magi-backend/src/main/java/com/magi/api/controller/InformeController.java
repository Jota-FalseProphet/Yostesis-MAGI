package com.magi.api.controller;

import com.magi.api.service.FiltroInforme.Formato;
import com.magi.api.service.InformeService;
import com.magi.api.util.PeriodoUtils.Tipo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final InformeService informeService;

    public InformeController(InformeService informeService) {
        this.informeService = informeService;
    }

    private Tipo mapPeriodo(String p) {
        if (p == null) return null;
        switch (p.toUpperCase(Locale.ROOT)) {
            case "SEMANA":
                return Tipo.SEMANA_ISO;       
            case "MES":
                return Tipo.MES;              
            case "TRIMESTRE":
                return Tipo.TRIMESTRE;        
            case "CURSO":
                return Tipo.CURSO_ESCOLAR;    
            default:
                return null;                 
        }
    }

    @GetMapping("/faltas")
    public ResponseEntity<?> obtenerInforme(
            @RequestParam(required = false) String periodo,          
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

       
        Tipo tipoPeriodo = mapPeriodo(periodo);
        if (periodo != null && tipoPeriodo == null) {
            return ResponseEntity.badRequest()
                    .body("Valor inválido para 'periodo'. " +
                          "Valores admitidos: SEMANA, MES, TRIMESTRE, CURSO");
        }

       
        if (formato == Formato.JSON) {
            if (tipoPeriodo != null && ref != null) {
                Map<String, LocalDate> rango =
                        com.magi.api.util.PeriodoUtils.calcular(tipoPeriodo, ref);
                return ResponseEntity.ok(
                        informeService.obtenerDetalle(
                                rango.get("desde"), rango.get("hasta"),
                                idDocente, idGrupo));
            }
            
            return ResponseEntity.ok(
                    informeService.obtenerDetalle(desde, hasta, idDocente, idGrupo));
        }

       
        if (formato == Formato.PDF) {
            ByteArrayResource file;
            if (tipoPeriodo != null && ref != null) {
                file = informeService.generarPorPeriodo(
                        tipoPeriodo, ref, idDocente, idGrupo, formato);
            } else {
               
                com.magi.api.service.FiltroInforme f = new com.magi.api.service.FiltroInforme();
                f.setDesde(desde);
                f.setHasta(hasta);
                f.setIdDocente(idDocente);
                f.setIdGrupo(idGrupo);
                f.setFormato(formato);
                file = informeService.generarInforme(f);
            }

            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_PDF);
            h.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("faltas_" + LocalDate.now() + ".pdf")
                            .build());

            return new ResponseEntity<>(file, h, HttpStatus.OK);
        }

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Formato no soportado: solo JSON o PDF.");
    }
}
