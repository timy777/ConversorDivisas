package com.conversor.divisas.diplomado.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para devolver el resultado de una conversión
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private double montoOriginal;
    private String monedaOrigen;
    private double montoConvertido;
    private String monedaDestino;
    private double tasa;
    private long timestamp;
}
