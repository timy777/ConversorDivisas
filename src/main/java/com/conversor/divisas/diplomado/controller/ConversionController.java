package com.conversor.divisas.diplomado.controller;

import com.conversor.divisas.diplomado.model.Moneda;
import com.conversor.divisas.diplomado.model.ConversionRequest;
import com.conversor.divisas.diplomado.model.ConversionResponse;
import com.conversor.divisas.diplomado.service.ConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para operaciones de conversión de divisas
 * Endpoints disponibles:
 * - GET /api/v1/monedas - Obtener todas las monedas
 * - GET /api/v1/monedas/{codigo} - Obtener info de una moneda
 * - POST /api/v1/convertir - Convertir entre monedas
 */
@RestController
@RequestMapping("/api/v1")
public class ConversionController {

    @Autowired
    private ConversionService conversionService;

    /**
     * GET /api/v1/health - Verificar que el servicio está activo
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Conversor de Divisas",
            "version", "1.0.0"
        ));
    }

    /**
     * GET /api/v1/monedas - Obtiene todas las monedas disponibles
     */
    @GetMapping("/monedas")
    public ResponseEntity<Map<String, Moneda>> obtenerMonedas() {
        try {
            Map<String, Moneda> monedas = conversionService.obtenerMonedas();
            return ResponseEntity.ok(monedas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/v1/monedas/{codigo} - Obtiene información de una moneda específica
     * Ejemplo: /api/v1/monedas/USD
     */
    @GetMapping("/monedas/{codigo}")
    public ResponseEntity<?> obtenerMoneda(@PathVariable String codigo) {
        try {
            Moneda moneda = conversionService.obtenerMoneda(codigo);
            if (moneda == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Moneda no encontrada: " + codigo));
            }
            return ResponseEntity.ok(moneda);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/convertir - Realiza conversión entre dos monedas
     * Body JSON:
     * {
     *   "monto": 100,
     *   "monedaOrigen": "USD",
     *   "monedaDestino": "EUR"
     * }
     */
    @PostMapping("/convertir")
    public ResponseEntity<?> convertir(@RequestBody ConversionRequest request) {
        try {
            // Validar entrada
            if (request.getMonto() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El monto debe ser mayor a 0"));
            }

            ConversionResponse response = conversionService.convertir(
                request.getMonto(),
                request.getMonedaOrigen(),
                request.getMonedaDestino()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error en la conversión: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/convertir/{monedaOrigen}/{monedaDestino}/{monto}
     * Alternativa simple usando path parameters
     * Ejemplo: /api/v1/convertir/USD/EUR/100
     */
    @GetMapping("/convertir/{monedaOrigen}/{monedaDestino}/{monto}")
    public ResponseEntity<?> convertirSimple(
            @PathVariable String monedaOrigen,
            @PathVariable String monedaDestino,
            @PathVariable double monto) {
        try {
            if (monto <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El monto debe ser mayor a 0"));
            }

            ConversionResponse response = conversionService.convertir(monto, monedaOrigen, monedaDestino);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error en la conversión: " + e.getMessage()));
        }
    }
}
