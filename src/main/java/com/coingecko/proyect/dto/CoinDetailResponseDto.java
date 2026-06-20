package com.coingecko.proyect.dto;

import java.util.List;
import java.util.Map;

public record CoinDetailResponseDto(
        String id,
        String name,
        String symbol,
        Map<String, Object> marketData, // Aquí Jackson meterá los precios actuales que vienen de CoinGecko
        List<List<Double>> chartPrices  // Aquí se meteran los precios de la gráfica que vienen de tu CoinChartDto
) {}