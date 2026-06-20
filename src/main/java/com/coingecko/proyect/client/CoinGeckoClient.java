package com.coingecko.proyect.client;

import com.coingecko.proyect.dto.CoinChartDto;
import com.coingecko.proyect.dto.CoinMarketDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class CoinGeckoClient {
    private final RestClient restClient;

    // Spring inyecta automáticamente el Bean que tú configuraste arriba
    public CoinGeckoClient(RestClient coinGeckoRestClient) {
        this.restClient = coinGeckoRestClient;
    }

    public List<CoinMarketDto> fetchMarkets(String vsCurrency, String ids) {
        return restClient.get()
                .uri("/coins/markets?vs_currency={currency}&ids={ids}&order=market_cap_desc", vsCurrency, ids)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CoinMarketDto>>() {});
    }

    public CoinChartDto coinChart(String id){
       return restClient.get()
               .uri("/coins/{id}/market_chart?vs_currency=usd&days=7&interval=daily", id)
                .retrieve()
                .body(CoinChartDto.class);
    }

    public Map<String, Object> fetchCoinData(String id) {
        return restClient.get()
                // Desactivamos datos comunitarios, de desarrolladores y localización para que la respuesta no sea gigante y rinda mejor
                .uri("/coins/{id}?localization=false&tickers=false&community_data=false&developer_data=false&sparkline=false", id)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Map<String, Double>> fetchSimplePrices(String fromCoin, String toCoin) {
        String ids = fromCoin + "," + toCoin;
        return restClient.get()
                .uri("/simple/price?ids={ids}&vs_currencies=usd", ids)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {});
    }


}
