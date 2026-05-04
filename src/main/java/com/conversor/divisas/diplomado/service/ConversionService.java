package com.conversor.divisas.diplomado.service;

import com.conversor.divisas.diplomado.model.Moneda;
import com.conversor.divisas.diplomado.model.ConversionResponse;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que maneja las conversiones de divisas
 * Utiliza tasas de cambio simuladas (en producción usaría una API externa)
 */
@Service
public class ConversionService {
    
    private final Map<String, Moneda> tasas;

    public ConversionService() {
        // Inicializar con tasas de cambio simuladas respecto a USD
        this.tasas = new HashMap<>();
        tasas.put("USD", new Moneda("USD", "Dólar Estadounidense", "$", 1.0));
        tasas.put("EUR", new Moneda("EUR", "Euro", "€", 0.92));
        tasas.put("GBP", new Moneda("GBP", "Libra Esterlina", "£", 0.79));
        tasas.put("CLP", new Moneda("CLP", "Peso Chileno", "$", 950.0));
        tasas.put("MXN", new Moneda("MXN", "Peso Mexicano", "$", 17.5));
        tasas.put("ARS", new Moneda("ARS", "Peso Argentino", "$", 900.0));
        tasas.put("BRL", new Moneda("BRL", "Real Brasileño", "R$", 5.0));
        tasas.put("JPY", new Moneda("JPY", "Yen Japonés", "¥", 150.0));
    }

    /**
     * Obtiene todas las monedas disponibles
     */
    public Map<String, Moneda> obtenerMonedas() {
        return new HashMap<>(tasas);
    }

    /**
     * Obtiene la información de una moneda específica
     */
    public Moneda obtenerMoneda(String codigo) {
        return tasas.getOrDefault(codigo.toUpperCase(), null);
    }

    /**
     * Realiza la conversión entre dos monedas
     */
    public ConversionResponse convertir(double monto, String monedaOrigen, String monedaDestino) {
        monedaOrigen = monedaOrigen.toUpperCase();
        monedaDestino = monedaDestino.toUpperCase();

        Moneda origen = tasas.get(monedaOrigen);
        Moneda destino = tasas.get(monedaDestino);

        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Moneda no soportada: " + 
                (origen == null ? monedaOrigen : monedaDestino));
        }

        // Convertir a USD primero, luego a la moneda destino
        double montoEnUSD = monto / origen.getTasa();
        double montoConvertido = montoEnUSD * destino.getTasa();
        
        // Tasa de cambio directa entre las dos monedas
        double tasa = destino.getTasa() / origen.getTasa();

        return new ConversionResponse(
            monto,
            monedaOrigen,
            montoConvertido,
            monedaDestino,
            tasa,
            System.currentTimeMillis()
        );
    }
}
