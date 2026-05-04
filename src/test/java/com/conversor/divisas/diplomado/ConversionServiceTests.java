package com.conversor.divisas.diplomado;

import com.conversor.divisas.diplomado.model.ConversionResponse;
import com.conversor.divisas.diplomado.model.Moneda;
import com.conversor.divisas.diplomado.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el servicio de conversión
 */
public class ConversionServiceTests {

    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionService();
    }

    @Test
    void testObtenerMonedas() {
        Map<String, Moneda> monedas = conversionService.obtenerMonedas();
        assertNotNull(monedas);
        assertTrue(monedas.containsKey("USD"));
        assertTrue(monedas.containsKey("EUR"));
        assertTrue(monedas.size() >= 8);
    }

    @Test
    void testObtenerMonedaExistente() {
        Moneda usd = conversionService.obtenerMoneda("USD");
        assertNotNull(usd);
        assertEquals("USD", usd.getCodigo());
        assertEquals("Dólar Estadounidense", usd.getNombre());
        assertEquals(1.0, usd.getTasa());
    }

    @Test
    void testObtenerMonedaNoExistente() {
        Moneda moneda = conversionService.obtenerMoneda("XYZ");
        assertNull(moneda);
    }

    @Test
    void testConvertirUSDaEUR() {
        ConversionResponse response = conversionService.convertir(100, "USD", "EUR");
        assertNotNull(response);
        assertEquals(100.0, response.getMontoOriginal());
        assertEquals("USD", response.getMonedaOrigen());
        assertEquals("EUR", response.getMonedaDestino());
        assertTrue(response.getMontoConvertido() > 0);
        assertTrue(response.getTasa() > 0);
    }

    @Test
    void testConvertirSameCurrency() {
        ConversionResponse response = conversionService.convertir(100, "USD", "USD");
        assertEquals(100.0, response.getMontoConvertido(), 0.01);
        assertEquals(1.0, response.getTasa());
    }

    @Test
    void testConvertirCaseInsensitive() {
        ConversionResponse response1 = conversionService.convertir(100, "USD", "EUR");
        ConversionResponse response2 = conversionService.convertir(100, "usd", "eur");
        assertEquals(response1.getMontoConvertido(), response2.getMontoConvertido(), 0.01);
    }

    @Test
    void testConvertirMonedaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertir(100, "XYZ", "USD");
        });
    }

    @Test
    void testConvertirAmbasMonedosInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertir(100, "ABC", "XYZ");
        });
    }

    @Test
    void testConversionMultiple() {
        // Convertir USD -> EUR -> GBP
        ConversionResponse step1 = conversionService.convertir(100, "USD", "EUR");
        ConversionResponse step2 = conversionService.convertir(step1.getMontoConvertido(), "EUR", "GBP");
        
        // Convertir directamente USD -> GBP
        ConversionResponse direct = conversionService.convertir(100, "USD", "GBP");
        
        // Los resultados deberían ser similares (con pequeña tolerancia por redondeo)
        assertEquals(direct.getMontoConvertido(), step2.getMontoConvertido(), 0.1);
    }
}
