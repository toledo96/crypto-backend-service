package com.coingecko.proyect.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Value("${coingecko.api-key}")
    String key;


    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.coingecko.com/api/v3")
                .defaultHeaders(httpHeaders -> httpHeaders.add("x-cg-demo-api-key",key))
                .defaultHeader("Accept", "application/json")
                .build();
    }

}
