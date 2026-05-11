package com.conversor.divisas.diplomado.service;

import com.conversor.divisas.diplomado.model.Moneda;
import com.conversor.divisas.diplomado.model.ConversionResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio que maneja las conversiones de divisas.
 *
 * Mantiene en memoria:
 *  - El catálogo de monedas (CRUD).
 *  - El historial de conversiones realizadas.
 *  - Estadísticas agregadas de uso.
 *
 * Las tasas iniciales son simuladas; en producción se consumiría una API externa.
 */
@Service
public class ConversionService {

    private final Map<String, Moneda> tasas = new ConcurrentHashMap<>();
    private final List<ConversionResponse> historial = new CopyOnWriteArrayList<>();
    private final Map<String, AtomicLong> usoPorMoneda = new ConcurrentHashMap<>();

    public ConversionService() {
        cargarMonedasPorDefecto();
    }

    private void cargarMonedasPorDefecto() {
        tasas.put("USD", new Moneda("USD", "Dólar Estadounidense", "$", 1.0));
        tasas.put("EUR", new Moneda("EUR", "Euro", "€", 0.92));
        tasas.put("GBP", new Moneda("GBP", "Libra Esterlina", "£", 0.79));
        tasas.put("CLP", new Moneda("CLP", "Peso Chileno", "$", 950.0));
        tasas.put("MXN", new Moneda("MXN", "Peso Mexicano", "$", 17.5));
        tasas.put("ARS", new Moneda("ARS", "Peso Argentino", "$", 900.0));
        tasas.put("BRL", new Moneda("BRL", "Real Brasileño", "R$", 5.0));
        tasas.put("JPY", new Moneda("JPY", "Yen Japonés", "¥", 150.0));
        tasas.put("BOB", new Moneda("BOB", "Boliviano", "Bs", 6.96));
    }

    public Map<String, Moneda> obtenerMonedas() {
        return new HashMap<>(tasas);
    }

    public Moneda obtenerMoneda(String codigo) {
        if (codigo == null) {
            return null;
        }
        return tasas.getOrDefault(codigo.toUpperCase(), null);
    }

    /**
     * Crea una nueva moneda en memoria. Falla si ya existe o si los datos son inválidos.
     */
    public Moneda crearMoneda(Moneda moneda) {
        validarMoneda(moneda);
        String codigo = moneda.getCodigo().toUpperCase();
        if (tasas.containsKey(codigo)) {
            throw new IllegalArgumentException("La moneda ya existe: " + codigo);
        }
        moneda.setCodigo(codigo);
        tasas.put(codigo, moneda);
        return moneda;
    }

    /**
     * Actualiza una moneda existente. Falla si no existe.
     */
    public Moneda actualizarMoneda(String codigo, Moneda datos) {
        if (codigo == null) {
            throw new IllegalArgumentException("Código de moneda requerido");
        }
        String key = codigo.toUpperCase();
        if (!tasas.containsKey(key)) {
            throw new IllegalArgumentException("Moneda no encontrada: " + key);
        }
        if (datos == null) {
            throw new IllegalArgumentException("Datos de moneda requeridos");
        }
        if (datos.getTasa() <= 0) {
            throw new IllegalArgumentException("La tasa debe ser mayor a 0");
        }
        Moneda actual = tasas.get(key);
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            actual.setNombre(datos.getNombre());
        }
        if (datos.getSimbolo() != null && !datos.getSimbolo().isBlank()) {
            actual.setSimbolo(datos.getSimbolo());
        }
        actual.setTasa(datos.getTasa());
        tasas.put(key, actual);
        return actual;
    }

    /**
     * Elimina una moneda del catálogo. Retorna true si se eliminó.
     */
    public boolean eliminarMoneda(String codigo) {
        if (codigo == null) {
            return false;
        }
        return tasas.remove(codigo.toUpperCase()) != null;
    }

    /**
     * Realiza la conversión entre dos monedas y registra el movimiento en el historial.
     */
    public ConversionResponse convertir(double monto, String monedaOrigen, String monedaDestino) {
        if (monedaOrigen == null || monedaDestino == null) {
            throw new IllegalArgumentException("Las monedas son obligatorias");
        }
        monedaOrigen = monedaOrigen.toUpperCase();
        monedaDestino = monedaDestino.toUpperCase();

        Moneda origen = tasas.get(monedaOrigen);
        Moneda destino = tasas.get(monedaDestino);

        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Moneda no soportada: " +
                (origen == null ? monedaOrigen : monedaDestino));
        }

        double montoEnUSD = monto / origen.getTasa();
        double montoConvertido = montoEnUSD * destino.getTasa();
        double tasa = destino.getTasa() / origen.getTasa();

        ConversionResponse response = new ConversionResponse(
            monto,
            monedaOrigen,
            montoConvertido,
            monedaDestino,
            tasa,
            System.currentTimeMillis()
        );

        historial.add(response);
        usoPorMoneda.computeIfAbsent(monedaOrigen, k -> new AtomicLong()).incrementAndGet();
        usoPorMoneda.computeIfAbsent(monedaDestino, k -> new AtomicLong()).incrementAndGet();

        return response;
    }

    /**
     * Devuelve una copia inmutable del historial de conversiones (más reciente al final).
     */
    public List<ConversionResponse> obtenerHistorial() {
        return Collections.unmodifiableList(new ArrayList<>(historial));
    }

    /**
     * Limpia el historial de conversiones y los contadores asociados.
     */
    public int limpiarHistorial() {
        int total = historial.size();
        historial.clear();
        usoPorMoneda.clear();
        return total;
    }

    /**
     * Devuelve estadísticas agregadas de las conversiones realizadas.
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalMonedas", tasas.size());
        stats.put("totalConversiones", historial.size());

        Map<String, Long> uso = new LinkedHashMap<>();
        usoPorMoneda.entrySet().stream()
            .sorted(Map.Entry.<String, AtomicLong>comparingByValue(
                Comparator.comparingLong(AtomicLong::get)).reversed())
            .forEach(e -> uso.put(e.getKey(), e.getValue().get()));
        stats.put("usoPorMoneda", uso);

        stats.put("monedaMasUsada", uso.isEmpty() ? null : uso.keySet().iterator().next());
        return stats;
    }

    private void validarMoneda(Moneda moneda) {
        if (moneda == null) {
            throw new IllegalArgumentException("La moneda no puede ser nula");
        }
        if (moneda.getCodigo() == null || moneda.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código de moneda es obligatorio");
        }
        if (moneda.getNombre() == null || moneda.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de moneda es obligatorio");
        }
        if (moneda.getTasa() <= 0) {
            throw new IllegalArgumentException("La tasa debe ser mayor a 0");
        }
    }
}
