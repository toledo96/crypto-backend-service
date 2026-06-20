package com.coingecko.proyect.config;

/*
Es una pésima práctica dejar que CompletableFuture use el pool por defecto de Java (ForkJoinPool.commonPool()) porque
un cuello de botella con la API externa podría congelar toda la aplicación, por eso se crea esta clase.
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "cryptoCoinExecutor")
    public Executor cryptoCoinExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configuraciones ideales para peticiones I/O (HTTP)
        executor.setCorePoolSize(5);       // Hilos mínimos que siempre estarán vivos
        executor.setMaxPoolSize(12);       // Hilos máximos si la cola se llena
        executor.setQueueCapacity(50);     // Cuántas peticiones acumular antes de crear más hilos
        executor.setThreadNamePrefix("CryptoAsync-"); // Prefijo para identificar los hilos en consola
        executor.initialize();

        return executor;
    }

}
