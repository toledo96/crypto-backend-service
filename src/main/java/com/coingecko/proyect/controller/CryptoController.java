package com.coingecko.proyect.controller;


import com.coingecko.proyect.dto.CoinDetailResponseDto;
import com.coingecko.proyect.dto.CoinMarketDto;
import com.coingecko.proyect.dto.ConversionResponseDto;
import com.coingecko.proyect.service.CryptoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/crypto")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:5173}")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/markets")
    public ResponseEntity<List<CoinMarketDto>> getMarkets(@RequestParam(defaultValue = "usd") String currency) {
        List<CoinMarketDto> response = cryptoService.getMarketDataConcurrent(currency);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/coins/{coinId}/details")
    public ResponseEntity<CoinDetailResponseDto> getCoinsDetails(@PathVariable String coinId){
        CoinDetailResponseDto response = cryptoService.getCointDetailsAndChart(coinId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/converter")
    public ResponseEntity<ConversionResponseDto> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        ConversionResponseDto response = cryptoService.convertCrypto(from, to, amount);
        return ResponseEntity.ok(response);
    }
}