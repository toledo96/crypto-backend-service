package com.coingecko.proyect.service;

import com.coingecko.proyect.client.CoinGeckoClient;
import com.coingecko.proyect.dto.CoinChartDto;
import com.coingecko.proyect.dto.CoinDetailResponseDto;
import com.coingecko.proyect.dto.CoinMarketDto;
import com.coingecko.proyect.dto.ConversionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CoinGeckoClient coinGeckoClient;

    @Qualifier("cryptoCoinExecutor")
    private final Executor executor;

    /**
     * Busca en Redis bajo la zona "marketDashboard".
     * Si no existe el registro en caché, ejecuta este bloque concurrentemente.
     */
    @Cacheable(value = "marketDashboard", key = "#vsCurrency")
    public List<CoinMarketDto> getMarketDataConcurrent(String vsCurrency) {

        // 1. Tarea Asíncrona A: Buscar un set de monedas (ej. bitcoin y ethereum)
        CompletableFuture<List<CoinMarketDto>> primaryCoinsFuture = CompletableFuture.supplyAsync(
                () -> coinGeckoClient.fetchMarkets(vsCurrency, "bitcoin,ethereum"), executor);

        // 2. Tarea Asíncrona B: Buscar otro set de monedas (ej. solana y cardano)
        CompletableFuture<List<CoinMarketDto>> secondaryCoinsFuture = CompletableFuture.supplyAsync(
                () -> coinGeckoClient.fetchMarkets(vsCurrency, "solana,cardano"), executor);

        // 3. Bloqueo controlado: Esperar a que AMBOS hilos terminen sus llamadas HTTP en paralelo
        CompletableFuture.allOf(primaryCoinsFuture, secondaryCoinsFuture).join();

        List<CoinMarketDto> consolidatedList = new ArrayList<>();
        try {
            // 4. Unir los resultados obtenidos de ambos hilos
            consolidatedList.addAll(primaryCoinsFuture.get());
            consolidatedList.addAll(secondaryCoinsFuture.get());
        } catch (Exception e) {
            throw new RuntimeException("Error procesando las respuestas asíncronas de CoinGecko", e);
        }

        return consolidatedList;
    }


    @Cacheable(value = "coinDetails", key = "#coinid", condition="#coinid!=null")
    public CoinDetailResponseDto getCointDetailsAndChart(String coinId){
        // 1. Hilo A: Va a buscar los datos generales de la moneda (devuelve un Map flexible)
        CompletableFuture<Map<String,Object>> coinDataFuture = CompletableFuture.supplyAsync(
                () -> coinGeckoClient.fetchCoinData(coinId), executor);

        // 2. Hilo B: Va a buscar la gráfica (devuelve tu CoinChartDto)
        CompletableFuture<CoinChartDto> chartFuture = CompletableFuture.supplyAsync(
                () -> coinGeckoClient.coinChart(coinId), executor);

        // 3. Esperamos a que ambos hilos terminen en paralelo
        CompletableFuture.allOf(coinDataFuture,chartFuture).join();

        try{
            Map<String,Object> coinData = coinDataFuture.get();
            CoinChartDto chartData = chartFuture.get();

            // Extraemos el pedazo del JSON que tiene los precios actuales de la moneda
            Map<String, Object> marketData = (Map<String, Object>) coinData.get("market_data");

            // Usamos un Dto para meter los datos
            return new CoinDetailResponseDto(
                    (String) coinData.get("id"),
                    (String) coinData.get("name"),
                    (String) coinData.get("symbol"),
                    marketData,
                    chartData.prices()  // <--- Sacamos la lista de la gráfica de tu CoinChartDto
            );


        } catch (Exception e) {
            throw new RuntimeException("Error al consolidar los datos de: " + coinId , e);
        }

    }

    @Cacheable(value = "cryptoConversion", key = "#fromCoin + '-' + #toCoin + '-' + #amount")
    public ConversionResponseDto convertCrypto(String fromCoin, String toCoin, BigDecimal amount) {

        Map<String,Map<String,Double>> prices = coinGeckoClient.fetchSimplePrices(fromCoin, toCoin);

        try {
            // 2. Extraemos los precios de forma segura
            Double fromPriceUsd = prices.get(fromCoin.toLowerCase()).get("usd");
            Double toPriceUsd = prices.get(toCoin.toLowerCase()).get("usd");

            // 3. Hacemos la regla de 3 con BigDecimal para no perder decimales
            // Fórmula: (Monto * PrecioOrigen) / PrecioDestino
            BigDecimal fromPrice = BigDecimal.valueOf(fromPriceUsd);
            BigDecimal toPrice = BigDecimal.valueOf(toPriceUsd);

            // Calculamos el resultado con una precisión de 8 decimales (estándar cripto)
            BigDecimal result = amount.multiply(fromPrice)
                    .divide(toPrice, 8, java.math.RoundingMode.HALF_UP);

            // 4. Armamos la respuesta
            return new ConversionResponseDto(
                    fromCoin,
                    toCoin,
                    amount,
                    result,
                    System.currentTimeMillis()
            );

        } catch (NullPointerException e) {
            throw new RuntimeException("No se pudieron obtener las cotizaciones para una de las monedas: " + fromCoin + " o " + toCoin);
        }

    }

}
