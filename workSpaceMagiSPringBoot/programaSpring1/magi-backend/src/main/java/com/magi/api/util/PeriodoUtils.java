package com.magi.api.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

public class PeriodoUtils {

    public enum Tipo { DIA, SEMANA_ISO, MES, TRIMESTRE, CURSO_ESCOLAR }

   
    public static Map<String, LocalDate> calcular(Tipo tipo, LocalDate referencia) {
        LocalDate desde, hasta;

        switch (tipo) {
            case DIA -> {
                desde = hasta = referencia;
            }
            case SEMANA_ISO -> {
                desde = referencia.with(java.time.DayOfWeek.MONDAY);
                hasta = desde.plusDays(6);
            }
            case MES -> {
                desde = referencia.withDayOfMonth(1);
                hasta = referencia.with(TemporalAdjusters.lastDayOfMonth());
            }
            case TRIMESTRE -> {
                int mes = ((referencia.getMonthValue() - 1) / 3) * 3 + 1; 
                desde = LocalDate.of(referencia.getYear(), mes, 1);
                hasta = desde.plusMonths(3).minusDays(1);
            }
            case CURSO_ESCOLAR -> {          
                if (referencia.getMonthValue() >= 9) {       
                    desde = LocalDate.of(referencia.getYear(), 9, 1);
                    hasta = LocalDate.of(referencia.getYear() + 1, 6, 30);
                } else {                                     
                    desde = LocalDate.of(referencia.getYear() - 1, 9, 1);
                    hasta = LocalDate.of(referencia.getYear(), 6, 30);
                }
            }
            default -> throw new IllegalArgumentException("Tipo no soportado");
        }
        return Map.of("desde", desde, "hasta", hasta);
    }
}
