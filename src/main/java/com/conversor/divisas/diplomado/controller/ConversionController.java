package com.conversor.divisas.diplomado.controller;

import com.conversor.divisas.diplomado.model.Moneda;
import com.conversor.divisas.diplomado.model.ConversionRequest;
import com.conversor.divisas.diplomado.model.ConversionResponse;
import com.conversor.divisas.diplomado.service.ConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones de conversión de divisas.
 *
 * Endpoints disponibles:
 *  - GET    /api/v1/health
 *  - GET    /api/v1/monedas
 *  - GET    /api/v1/monedas/{codigo}
 *  - POST   /api/v1/monedas
 *  - PUT    /api/v1/monedas/{codigo}
 *  - DELETE /api/v1/monedas/{codigo}
 *  - POST   /api/v1/convertir
 *  - GET    /api/v1/convertir/{monedaOrigen}/{monedaDestino}/{monto}
 *  - GET    /api/v1/convertir/historial
 *  - DELETE /api/v1/convertir/historial
 *  - GET    /api/v1/estadisticas
 */
@RestController
@RequestMapping("/api/v1")
public class ConversionController {

    @Autowired
    private ConversionService conversionService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Conversor de Divisas",
            "version", "1.1.0"
        ));
    }

    @GetMapping("/monedas")
    public ResponseEntity<Map<String, Moneda>> obtenerMonedas() {
        try {
            return ResponseEntity.ok(conversionService.obtenerMonedas());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
     * Crea una nueva moneda. Si el código ya existe, devuelve 409.
     */
    @PostMapping("/monedas")
    public ResponseEntity<?> crearMoneda(@RequestBody Moneda moneda) {
        try {
            Moneda creada = conversionService.crearMoneda(moneda);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().contains("ya existe")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualiza la tasa, nombre o símbolo de una moneda existente.
     */
    @PutMapping("/monedas/{codigo}")
    public ResponseEntity<?> actualizarMoneda(@PathVariable String codigo,
                                              @RequestBody Moneda datos) {
        try {
            Moneda actualizada = conversionService.actualizarMoneda(codigo, datos);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().contains("no encontrada")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Elimina una moneda del catálogo en memoria.
     */
    @DeleteMapping("/monedas/{codigo}")
    public ResponseEntity<?> eliminarMoneda(@PathVariable String codigo) {
        try {
            boolean eliminado = conversionService.eliminarMoneda(codigo);
            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Moneda no encontrada: " + codigo));
            }
            return ResponseEntity.ok(Map.of(
                "mensaje", "Moneda eliminada",
                "codigo", codigo.toUpperCase()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/convertir")
    public ResponseEntity<?> convertir(@RequestBody ConversionRequest request) {
        try {
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

    /**
     * Devuelve el historial de conversiones realizadas en memoria.
     */
    @GetMapping("/convertir/historial")
    public ResponseEntity<List<ConversionResponse>> obtenerHistorial() {
        return ResponseEntity.ok(conversionService.obtenerHistorial());
    }

    /**
     * Limpia el historial de conversiones.
     */
    @DeleteMapping("/convertir/historial")
    public ResponseEntity<Map<String, Object>> limpiarHistorial() {
        int eliminadas = conversionService.limpiarHistorial();
        return ResponseEntity.ok(Map.of(
            "mensaje", "Historial limpiado",
            "registrosEliminados", eliminadas
        ));
    }

    /**
     * Devuelve estadísticas agregadas: total de monedas, total de conversiones,
     * uso por moneda y la moneda más utilizada.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(conversionService.obtenerEstadisticas());
    }
}
