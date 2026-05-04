package com.conversor.divisas.diplomado.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir solicitudes de conversión
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {
    private double monto;
    private String monedaOrigen;   // Ej: USD
    private String monedaDestino;  // Ej: EUR
}
