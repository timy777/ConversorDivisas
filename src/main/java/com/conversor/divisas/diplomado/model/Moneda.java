package com.conversor.divisas.diplomado.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa una moneda con su código y símbolo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moneda {
    private String codigo;    // USD, EUR, CLP, etc.
    private String nombre;    // Dólar Estadounidense, Euro, Peso Chileno
    private String simbolo;   // $, €, £, etc.
    private double tasa;      // Tasa de cambio respecto a USD
}
