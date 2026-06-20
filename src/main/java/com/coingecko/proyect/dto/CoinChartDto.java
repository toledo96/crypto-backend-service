package com.coingecko.proyect.dto;

import java.util.List;

public record CoinChartDto(
        List<List<Double>> prices) {


}
