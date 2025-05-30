package com.magi.api.service;

import com.magi.api.model.FaltaDetalle;
import com.magi.api.model.FaltaResumen;
import com.magi.api.repository.FaltaDetalleRepository;
import com.magi.api.repository.FaltaResumenRepository;
import com.magi.api.service.FiltroInforme.Formato;
import com.magi.api.util.PeriodoUtils;
import com.magi.api.util.PeriodoUtils.Tipo;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class InformeService {
    private static final Logger log = LoggerFactory.getLogger(InformeService.class);
    private static final Logger auditLog = LoggerFactory.getLogger("AUDITORIA");

    private final FaltaDetalleRepository detalleRepo;
    private final FaltaResumenRepository resumenRepo;

    private JasperReport jasperPlantilla;
    private boolean pdfDisponible = true;

    public InformeService(FaltaDetalleRepository detalleRepo,
                          FaltaResumenRepository resumenRepo) {
        this.detalleRepo = detalleRepo;
        this.resumenRepo = resumenRepo;
    }

    @PostConstruct
    private void init() {
        try (InputStream is = getClass().getResourceAsStream("/reportes/faltas.jrxml")) {
            if (is == null) {
                log.warn("Plantilla Jasper '/reportes/faltas.jrxml' no encontrada; se desactiva exportación PDF.");
                pdfDisponible = false;
                return;
            }
            jasperPlantilla = JasperCompileManager.compileReport(is);
        } catch (IOException | JRException e) {
            log.error("Error cargando plantilla Jasper, desactivando PDF export.", e);
            pdfDisponible = false;
        }
    }

    public ByteArrayResource generarInforme(FiltroInforme f) throws Exception {
        List<FaltaDetalle> detalle = detalleRepo.buscarDetalle(
                f.getDesde(), f.getHasta(), f.getIdDocente(), f.getIdGrupo());

        auditLog.info("INFORME faltas {} → filas:{}, docente:{}, grupo:{}",
                LocalDate.now(), detalle.size(), f.getIdDocente(), f.getIdGrupo());

        switch (f.getFormato()) {
            case PDF:
                if (!pdfDisponible) {
                    throw new IllegalStateException("Exportación PDF no disponible");
                }
                return exportarPdf(detalle, f);
            case XLSX:
                return exportarExcel(detalle, f);
            default:
                return null; // JSON será manejado por el controller
        }
    }

    private ByteArrayResource exportarPdf(List<FaltaDetalle> data, FiltroInforme f) throws JRException {
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(data);
        Map<String,Object> params = new HashMap<>();
        params.put("FECHA_EMISION", LocalDate.now());
        params.put("RANGO",
                (f.getDesde()!=null?f.getDesde():"∞") + " ➜ " + (f.getHasta()!=null?f.getHasta():"∞"));

        JasperPrint print = JasperFillManager.fillReport(jasperPlantilla, params, ds);
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(print);
        return new ByteArrayResource(pdfBytes);
    }

    private ByteArrayResource exportarExcel(List<FaltaDetalle> data, FiltroInforme f) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Faltas");
            int rowIdx = 0;
            Row head = sheet.createRow(rowIdx++);
            String[] cols = {"Fecha","Docente","Grupo","Hora desde","Hora fins","Guardia cubierta"};
            for (int c=0; c<cols.length; c++) {
                head.createCell(c).setCellValue(cols[c]);
            }
            for (FaltaDetalle fd : data) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(fd.getFecha().toString());
                r.createCell(1).setCellValue(fd.getDocente());
                r.createCell(2).setCellValue(fd.getGrupo());
                r.createCell(3).setCellValue(fd.getHoraDesde().toString());
                r.createCell(4).setCellValue(fd.getHoraFins().toString());
                r.createCell(5).setCellValue(fd.isCubierta() ? "Sí" : "No");
            }
            wb.write(out);
            return new ByteArrayResource(out.toByteArray());
        }
    }

    public ByteArrayResource generarPorPeriodo(Tipo tipo, LocalDate ref, Integer idDocente, Integer idGrupo, Formato formato) throws Exception {
        Map<String,LocalDate> p = PeriodoUtils.calcular(tipo, ref);
        FiltroInforme f = new FiltroInforme();
        f.setDesde(p.get("desde"));
        f.setHasta(p.get("hasta"));
        f.setIdDocente(idDocente);
        f.setIdGrupo(idGrupo);
        f.setFormato(formato);
        return generarInforme(f);
    }

    public List<FaltaResumen> obtenerResumenMensual(LocalDate mes) {
        return resumenRepo.findByMes(mes.withDayOfMonth(1));
    }

    @Transactional(readOnly = true)
    public List<FaltaDetalle> obtenerDetalle(LocalDate desde,
                                             LocalDate hasta,
                                             Integer idDocente,
                                             Integer idGrupo) {
        return detalleRepo.buscarDetalle(desde, hasta, idDocente, idGrupo);
    }

}
