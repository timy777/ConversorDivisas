package com.conversor.divisas.diplomado;

import com.conversor.divisas.diplomado.controller.ConversionController;
import com.conversor.divisas.diplomado.service.ConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para el controlador de conversión
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ConversionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversionService conversionService;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("Conversor de Divisas"));
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
            .contentType(MediaType.APPLICATION_JSON)
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
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}
