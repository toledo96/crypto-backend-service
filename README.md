# Crypto-Backend-Service 

Este es el motor de servicios backend para la plataforma de criptomonedas. Está desarrollado con **Spring Boot 4**, 
diseñado bajo una arquitectura limpia en tres capas y optimizado para la agregación de APIs externas utilizando programación concurrente y almacenamiento 
en caché de alto rendimiento.

## 🚀 Características Principales

* **Arquitectura BFF (Backend for Frontend):** Centraliza y unifica múltiples peticiones hacia la API externa de CoinGecko para entregar un objeto de datos masticado y optimizado al cliente.
* **Concurrencia Asíncrona:** Implementación de `CompletableFuture` junto con un pool de hilos personalizado (`ThreadPoolTaskExecutor`) para paralelizar peticiones HTTP, reduciendo los tiempos de respuesta a la mitad.
* **Estrategia de Caché Avanzada (Redis):** Reducción de latencia y protección de cuotas de API mediante políticas de caché segmentadas por caso de uso (ej. TTLs cortos para conversión en tiempo real, TTLs largos para mercados).
* **Precisión Financiera:** Uso estricto de `BigDecimal` para todas las operaciones de conversión, garantizando la exactitud de los decimales en el cálculo de activos.

---

## 🛠️ Stack Tecnológico

* **Java 21**
* **Spring Boot 4** (Spring Web, Spring Data Redis)
* **Redis** (Gestión de Caché en memoria)
* **RestClient** (Cliente HTTP síncrono/asíncrono de Spring)
* **Docker** (Para entorno local de Redis)

---

## 📐 Arquitectura y Flujo de Datos

El proyecto sigue el flujo clásico de desacoplamiento de responsabilidades:



1.  **Controller:** Expone los endpoints REST limpios usando variables de ruta y query params.
2.  **Service (Orquestador):** Contiene la lógica de negocio, gestiona el pool de hilos con `CompletableFuture.allOf().join()` y unifica las respuestas en moldes inmutables (`Records` de Java).
3.  **Client:** Componente especializado que encapsula las llamadas HTTP crudas hacia los servidores de CoinGecko.

---

## 🔒 Variables de Entorno Requeridas

Para desplegar o correr este proyecto de forma local, es necesario configurar las siguientes variables de entorno (puedes inyectarlas en tu IDE o sistema operativo):

```yaml
SPRING_DATA_REDIS_HOST: localhost
SPRING_DATA_REDIS_PORT: 6179
COINGECKO_API_KEY: TU_API_KEY_AQUÍ
```

## 🔀 Endpoints de la API

### 1. Listado de Mercados
* **Ruta:** `GET /api/v1/crypto/markets`
* **Descripción:** Devuelve el top de criptomonedas con sus precios y tendencias. Almacenado en caché para optimizar el rendimiento.

![Evidencia Mercados](assets/markets_end_point.png)

### 2. Detalle Profundo y Gráfica
* **Ruta:** `GET /api/v1/crypto/coins/{id}/details`
* **Descripción:** Orquesta dos hilos en paralelo. Trae los datos básicos de la moneda (`/coins/{id}`) y su historial de precios de los últimos 7 días (`/market_chart`), fusionándolos en un solo DTO.

![Evidencia Detalle](assets/details_end_point.png)

### 3. Calculadora de Conversión
* **Ruta:** `GET /api/v1/crypto/converter`
* **Parámetros:** `from` (moneda origen), `to` (moneda destino), `amount` (monto).
* **Descripción:** Realiza la conversión matemática con precisión de 8 decimales usando `BigDecimal`. Cuenta con una política de caché de expiración rápida (TTL de 30 segundos) para simular cotizaciones en tiempo real.

![Evidencia Conversión](assets/converter_end_point.png)

---

## 🛠️ Instrucciones para Ejecución Local

1. **Crear cuenta en CoinGecko:** Obtener una API Key gratuita con el link https://www.coingecko.com/en/api/pricing 
   y seleccionar el plan gratuito.

![Evidencia Conversión](assets/coingecko_plan.png)


2. **Levantar Redis:** Asegúrate de tener Redis corriendo localmente (puedes usar Docker):
```bash
   docker run --name redis-crypto -p 6379:6379 -d redis
   ```

3. **Clonar el repositorio:**

 ```Bash
git clone [https://github.com/toledo96/crypto-backend-service.git]
 ```

4. **Configurar la API Key:** Añade tu token de CoinGecko en las variables de entorno de tu IDE.

 ```
    COINGECKO_API_KEY
 ```

5. **Ejecutar la aplicación:** Arranca el proyecto desde tu IDE o mediante la terminal con:

 ```Bash
./mvnw spring-boot:run
 ```

## 💾 Configuración y Monitoreo de Caché (Redis)

Para optimizar los tiempos de respuesta del microservicio y mitigar el límite de peticiones (rate limiting) de las APIs externas, el proyecto implementa un mecanismo de caché distribuida utilizando **Redis**.

### 🛠️ Arquitectura de la Caché
* **Redis Caching:** Almacenamiento temporal de consultas de alta demanda.
* **Resiliencia de Conexión:** Configuración externalizada con valores por defecto preparados para despliegues locales y entornos contenerizados.

### 📈 Métricas de Redis en Grafana
Al habilitar las métricas de Spring Boot, el tablero de Grafana monitorea automáticamente el comportamiento del cliente de Redis (Lettuce/Jedis) y el pool de conexiones:
* **HikariCP / Connection Pool:** Estado de las conexiones activas, inactivas y tiempos de espera para interactuar con la base de datos en memoria.
* **Cache Hits / Misses:** Volumen de consultas exitosas recuperadas desde la caché versus peticiones que requirieron golpear el backend o servicios externos.

### 🚀 Configuración del Entorno (`application.yml`)
La conectividad se gestiona de manera dinámica para facilitar la portabilidad del entorno de desarrollo a Docker:

```yaml
spring:
   data:
      redis:
         host: ${REDIS_HOST:localhost}
         port: ${REDIS_PORT:6379}
```

## 📊 Observabilidad y Monitoreo

El proyecto cuenta con una infraestructura completa de monitoreo para evaluar el rendimiento y la salud del microservicio en tiempo real utilizando el stack **Prometheus** y **Grafana**.

### 🛠️ Tecnologías Utilizadas
* **Spring Boot Actuator & Micrometer:** Para la exposición y recolección nativa de métricas de la JVM, Tomcat y conexiones.
* **Prometheus:** Servidor de series temporales encargado de realizar el *scraping* de las métricas expuestas.
* **Grafana:** Panel de control visual conectado a Prometheus para la representación gráfica del estado del sistema.

### 📈 Métricas Monitoreadas
* **JVM memory:** Uso y comportamiento de la memoria *Heap* y *Non-Heap*.
* **CPU Usage & Load Average:** Monitoreo del consumo de procesamiento del microservicio.
* **Tomcat Threads:** Hilos activos, ocupados y disponibles en el servidor embebido.
* **EasyBroker / Redis Integrations:** Comportamiento de las conexiones externas y almacenamiento en caché.

### 🚀 Cómo Levantar el Entorno de Monitoreo

1. **Asegurar las variables en el `application.yml`**
   El sistema inyecta automáticamente el tag de la aplicación para que Grafana lo reconozca de manera nativa:

```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health, info, prometheus
     metrics:
       tags:
         application: ${spring.application.name}
```

2. **Levantar los contenedores de Docker**
   Ejecuta el siguiente comando en la raíz del proyecto para iniciar Prometheus y Grafana en segundo plano:
```
docker compose up -d
```

3. **Acceso a herramientas**
   - Prometheus UI: http://localhost:9090 (Verifica el estado en Status -> Targets).
   - Grafana Dashboard: http://localhost:3000 (Importa el tablero con el ID 11378 y selecciona el datasource de Prometheus).
