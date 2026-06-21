package com.coingecko.proyect.service;

import com.coingecko.proyect.client.CoinGeckoClient;
import com.coingecko.proyect.dto.ConversionResponseDto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private CoinGeckoClient coinGeckoClient;

    @InjectMocks
    private CryptoService cryptoService;

    @Test
    @DisplayName("Deberia calcuar la conversión exacta en milisegundos con BigDecimal")
    void conversionCryptoExistosa(){
// 1. ARRANGE (Configuración del escenario)
        String fromCoin = "bitcoin";
        String toCoin = "usd";
        BigDecimal amount = new BigDecimal("2.5");

        // Construimos el mapa anidado exacto que simula la respuesta de CoinGecko
        Map<String, Map<String, Double>> mockPricesResponse = new HashMap<>();

        Map<String, Double> bitcoinPrices = new HashMap<>();
        bitcoinPrices.put("usd", 64000.0);
        mockPricesResponse.put("bitcoin", bitcoinPrices);

        Map<String, Double> usdPrices = new HashMap<>();
        usdPrices.put("usd", 1.0);
        mockPricesResponse.put("usd", usdPrices);

        // Estipulamos el comportamiento del Mock usando tu método real
        when(coinGeckoClient.fetchSimplePrices(fromCoin, toCoin)).thenReturn(mockPricesResponse);

        // 2. ACT (Ejecución del método de tu servicio)
        ConversionResponseDto response = cryptoService.convertCrypto(fromCoin, toCoin, amount);

        // 3. ASSERT (Validaciones)
        assertNotNull(response, "La respuesta no debería ser nula");
        assertEquals("bitcoin", response.fromCoin());
        assertEquals("usd", response.toCoin());

        // El cálculo esperado: 2.5 BTC * $64,000 = $160,000.00
        BigDecimal expectedResult = new BigDecimal("160000.00");

        // Usamos compareTo == 0 porque es la forma correcta y segura de comparar BigDecimals ignorando la escala de ceros
        assertEquals(0, expectedResult.compareTo(response.result()),
                "El cálculo matemático de conversión con BigDecimal falló o no dio el monto exacto");

        // Verificamos que el servicio realmente consumió el cliente una sola vez
        verify(coinGeckoClient, times(1)).fetchSimplePrices(fromCoin, toCoin);
    }

}