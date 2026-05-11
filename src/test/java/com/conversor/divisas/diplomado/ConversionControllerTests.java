package com.conversor.divisas.diplomado;

import com.conversor.divisas.diplomado.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de API (integración) para el controlador REST de conversión.
 * Verifican los endpoints completos con MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ConversionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversionService conversionService;

    @BeforeEach
    void resetEstadoEnMemoria() {
        // Limpia historial entre tests para que las estadísticas sean determinísticas
        conversionService.limpiarHistorial();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("Conversor de Divisas"))
            .andExpect(jsonPath("$.version").exists());
    }

    @Test
    void testObtenerMonedas() throws Exception {
        mockMvc.perform(get("/api/v1/monedas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.USD").exists())
            .andExpect(jsonPath("$.EUR").exists())
            .andExpect(jsonPath("$.USD.codigo").value("USD"));
    }

    @Test
    void testObtenerMonedaEspecifica() throws Exception {
        mockMvc.perform(get("/api/v1/monedas/USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigo").value("USD"))
            .andExpect(jsonPath("$.nombre").value("Dólar Estadounidense"));
    }

    @Test
    void testObtenerMonedaNoExistente() throws Exception {
        mockMvc.perform(get("/api/v1/monedas/XYZ"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testConvertirUSDtoEUR() throws Exception {
        String requestBody = "{\"monto\": 100, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"EUR\"}";

        mockMvc.perform(post("/api/v1/convertir")
            .contentType("application/json")
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.montoOriginal").value(100.0))
            .andExpect(jsonPath("$.monedaOrigen").value("USD"))
            .andExpect(jsonPath("$.monedaDestino").value("EUR"))
            .andExpect(jsonPath("$.montoConvertido").exists());
    }

    @Test
    void testConvertirSimpleURI() throws Exception {
        mockMvc.perform(get("/api/v1/convertir/USD/EUR/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.montoOriginal").value(100.0))
            .andExpect(jsonPath("$.monedaOrigen").value("USD"));
    }

    @Test
    void testConvertirMontoNegativo() throws Exception {
        String requestBody = "{\"monto\": -50, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"EUR\"}";

        mockMvc.perform(post("/api/v1/convertir")
            .contentType("application/json")
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testConvertirMonedaInexistenteRetorna400() throws Exception {
        String requestBody = "{\"monto\": 100, \"monedaOrigen\": \"USD\", \"monedaDestino\": \"ZZZ\"}";
        mockMvc.perform(post("/api/v1/convertir")
            .contentType("application/json")
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCrearMonedaEndpoint() throws Exception {
        // Asegurar limpieza por si quedó de un test previo
        conversionService.eliminarMoneda("PEN");

        String body = "{\"codigo\":\"PEN\",\"nombre\":\"Sol Peruano\",\"simbolo\":\"S/\",\"tasa\":3.75}";
        mockMvc.perform(post("/api/v1/monedas")
            .contentType("application/json")
            .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value("PEN"))
            .andExpect(jsonPath("$.tasa").value(3.75));

        mockMvc.perform(get("/api/v1/monedas/PEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Sol Peruano"));

        conversionService.eliminarMoneda("PEN");
    }

    @Test
    void testCrearMonedaDuplicadaRetorna409() throws Exception {
        String body = "{\"codigo\":\"USD\",\"nombre\":\"Otro Dólar\",\"simbolo\":\"$\",\"tasa\":1.0}";
        mockMvc.perform(post("/api/v1/monedas")
            .contentType("application/json")
            .content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCrearMonedaInvalidaRetorna400() throws Exception {
        String body = "{\"codigo\":\"\",\"nombre\":\"\",\"simbolo\":\"?\",\"tasa\":0}";
        mockMvc.perform(post("/api/v1/monedas")
            .contentType("application/json")
            .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testActualizarMonedaEndpoint() throws Exception {
        String body = "{\"codigo\":\"EUR\",\"nombre\":\"Euro Actualizado\",\"simbolo\":\"€\",\"tasa\":0.95}";
        mockMvc.perform(put("/api/v1/monedas/EUR")
            .contentType("application/json")
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tasa").value(0.95))
            .andExpect(jsonPath("$.nombre").value("Euro Actualizado"));
    }

    @Test
    void testActualizarMonedaInexistenteRetorna404() throws Exception {
        String body = "{\"codigo\":\"ZZZ\",\"nombre\":\"X\",\"simbolo\":\"?\",\"tasa\":1.0}";
        mockMvc.perform(put("/api/v1/monedas/ZZZ")
            .contentType("application/json")
            .content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testEliminarMonedaEndpoint() throws Exception {
        conversionService.crearMoneda(
            new com.conversor.divisas.diplomado.model.Moneda("TMP", "Temporal", "T", 1.0));

        mockMvc.perform(delete("/api/v1/monedas/TMP"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Moneda eliminada"))
            .andExpect(jsonPath("$.codigo").value("TMP"));

        mockMvc.perform(get("/api/v1/monedas/TMP"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarMonedaInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/api/v1/monedas/ZZZ"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testHistorialDespuesDeConvertir() throws Exception {
        mockMvc.perform(get("/api/v1/convertir/USD/EUR/100")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/convertir/USD/GBP/50")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/convertir/historial"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].monedaDestino").value("EUR"))
            .andExpect(jsonPath("$[1].monedaDestino").value("GBP"));
    }

    @Test
    void testLimpiarHistorialEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/convertir/USD/EUR/100")).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/convertir/historial"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").value("Historial limpiado"))
            .andExpect(jsonPath("$.registrosEliminados").value(1));

        mockMvc.perform(get("/api/v1/convertir/historial"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testEstadisticasEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/convertir/USD/EUR/100")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/convertir/USD/GBP/50")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/estadisticas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalConversiones").value(2))
            .andExpect(jsonPath("$.totalMonedas").exists())
            .andExpect(jsonPath("$.monedaMasUsada").value("USD"))
            .andExpect(jsonPath("$.usoPorMoneda.USD").value(2));
    }
}
