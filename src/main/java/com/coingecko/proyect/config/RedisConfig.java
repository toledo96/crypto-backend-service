package com.coingecko.proyect.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching // Habilita la abstracción de caché de Spring (@Cacheable, @CacheEvict, etc.)
public class RedisConfig {



    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {

        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);

        // 1-Configura el RedisCacheManager con las opciones predeterminadas
        RedisCacheConfiguration redisConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Configura el tiempo de vida de la caché (opcional)
                .disableCachingNullValues() // Evita almacenar valores nulos en la caché
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // Serializa las claves como cadenas
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));


        // 2-Configuraciones especificas y personalizadas para diferentes caches (opcional)
        Map<String,RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();

        // Se asigna que la cache expirará cada 5 minutos
        cacheConfigurationMap.put("coinDetails",RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));

        // La caché del dashboard/global expirará cada 2 minutos
        cacheConfigurationMap.put("marketDashboard",RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(2)));

        // 3-Crea el RedisCacheManager con la configuración predeterminada y las configuraciones específicas
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .withInitialCacheConfigurations(cacheConfigurationMap)
                .build();

    }

}
