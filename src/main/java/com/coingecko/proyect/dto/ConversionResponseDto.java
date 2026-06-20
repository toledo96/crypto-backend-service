package com.coingecko.proyect.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ConversionResponseDto(
        // Nota: Se usa BigDecimal, ya que es el estándar de la industria para dinero y cripto.
        String fromCoin,
        String toCoin,
        BigDecimal amount,
        BigDecimal result,
        long timestamp
) implements Serializable {}
