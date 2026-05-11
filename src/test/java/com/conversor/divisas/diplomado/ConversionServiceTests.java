package com.conversor.divisas.diplomado;

import com.conversor.divisas.diplomado.model.ConversionResponse;
import com.conversor.divisas.diplomado.model.Moneda;
import com.conversor.divisas.diplomado.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el servicio de conversión.
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
    void testConvertirAmbasMonedasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertir(100, "ABC", "XYZ");
        });
    }

    @Test
    void testConversionMultiple() {
        ConversionResponse step1 = conversionService.convertir(100, "USD", "EUR");
        ConversionResponse step2 = conversionService.convertir(step1.getMontoConvertido(), "EUR", "GBP");
        ConversionResponse direct = conversionService.convertir(100, "USD", "GBP");
        assertEquals(direct.getMontoConvertido(), step2.getMontoConvertido(), 0.1);
    }

    @Test
    void testCrearMoneda() {
        Moneda peru = new Moneda("PEN", "Sol Peruano", "S/", 3.75);
        Moneda creada = conversionService.crearMoneda(peru);
        assertNotNull(creada);
        assertEquals("PEN", creada.getCodigo());
        assertNotNull(conversionService.obtenerMoneda("PEN"));
    }

    @Test
    void testCrearMonedaCodigoEnMinusculasSeNormaliza() {
        Moneda nueva = new Moneda("pen", "Sol Peruano", "S/", 3.75);
        Moneda creada = conversionService.crearMoneda(nueva);
        assertEquals("PEN", creada.getCodigo());
    }

    @Test
    void testCrearMonedaDuplicadaFalla() {
        Moneda duplicada = new Moneda("USD", "Dólar duplicado", "$", 1.0);
        assertThrows(IllegalArgumentException.class, () -> conversionService.crearMoneda(duplicada));
    }

    @Test
    void testCrearMonedaInvalidaFalla() {
        Moneda invalida = new Moneda("", "Sin código", "?", 1.0);
        assertThrows(IllegalArgumentException.class, () -> conversionService.crearMoneda(invalida));

        Moneda tasaCero = new Moneda("XAF", "Franco CFA", "F", 0);
        assertThrows(IllegalArgumentException.class, () -> conversionService.crearMoneda(tasaCero));
    }

    @Test
    void testActualizarMoneda() {
        Moneda datos = new Moneda("EUR", "Euro actualizado", "€", 0.95);
        Moneda actualizada = conversionService.actualizarMoneda("EUR", datos);
        assertEquals(0.95, actualizada.getTasa());
        assertEquals("Euro actualizado", actualizada.getNombre());
    }

    @Test
    void testActualizarMonedaInexistenteFalla() {
        Moneda datos = new Moneda("ZZZ", "Inventada", "?", 1.0);
        assertThrows(IllegalArgumentException.class,
            () -> conversionService.actualizarMoneda("ZZZ", datos));
    }

    @Test
    void testActualizarMonedaConTasaInvalidaFalla() {
        Moneda datos = new Moneda("EUR", "Euro", "€", -1);
        assertThrows(IllegalArgumentException.class,
            () -> conversionService.actualizarMoneda("EUR", datos));
    }

    @Test
    void testEliminarMoneda() {
        boolean eliminado = conversionService.eliminarMoneda("JPY");
        assertTrue(eliminado);
        assertNull(conversionService.obtenerMoneda("JPY"));
    }

    @Test
    void testEliminarMonedaInexistente() {
        assertFalse(conversionService.eliminarMoneda("ZZZ"));
    }

    @Test
    void testHistorialSeRegistraTrasConvertir() {
        assertEquals(0, conversionService.obtenerHistorial().size());
        conversionService.convertir(100, "USD", "EUR");
        conversionService.convertir(50, "USD", "GBP");
        List<ConversionResponse> historial = conversionService.obtenerHistorial();
        assertEquals(2, historial.size());
        assertEquals("EUR", historial.get(0).getMonedaDestino());
        assertEquals("GBP", historial.get(1).getMonedaDestino());
    }

    @Test
    void testLimpiarHistorial() {
        conversionService.convertir(100, "USD", "EUR");
        conversionService.convertir(50, "USD", "GBP");
        int eliminadas = conversionService.limpiarHistorial();
        assertEquals(2, eliminadas);
        assertTrue(conversionService.obtenerHistorial().isEmpty());
    }

    @Test
    void testEstadisticasIniciales() {
        Map<String, Object> stats = conversionService.obtenerEstadisticas();
        assertEquals(0, stats.get("totalConversiones"));
        assertTrue(((int) stats.get("totalMonedas")) >= 8);
        assertNull(stats.get("monedaMasUsada"));
    }

    @Test
    void testEstadisticasConMonedaMasUsada() {
        conversionService.convertir(100, "USD", "EUR");
        conversionService.convertir(50, "USD", "GBP");
        conversionService.convertir(25, "EUR", "USD");
        Map<String, Object> stats = conversionService.obtenerEstadisticas();
        assertEquals(3, stats.get("totalConversiones"));
        assertEquals("USD", stats.get("monedaMasUsada"));
    }
}
