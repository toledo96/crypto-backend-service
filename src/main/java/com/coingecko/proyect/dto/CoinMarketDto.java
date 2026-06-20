package com.coingecko.proyect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record CoinMarketDto(
        String id,
        String symbol,
        String name,
        String image,

        // Nota: Se utiiza @JsonProperty cuando hay diferencia de nombres entre el API y los atributos
        @JsonProperty("current_price")
        Double currentPrice,

        @JsonProperty("market_cap")
        Long marketCap,

        @JsonProperty("market_cap_rank")
        Integer marketCapRank,

        @JsonProperty("price_change_percentage_24h")
        Double priceChangePercentage24h
) implements Serializable {
    // Implementar Serializable es una buena práctica para asegurar que Redis lo serialice correctamente como JSON
}