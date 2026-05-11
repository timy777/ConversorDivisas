# 🌍 Conversor de Divisas - API REST

Proyecto Java con Spring Boot que implementa una API REST para convertir
entre diferentes divisas internacionales.

## 📋 Información del Proyecto

- **Lenguaje**: Java 21 LTS
- **Framework**: Spring Boot 3.4.3
- **Build Tool**: Maven
- **CI/CD**: GitHub Actions + Azure DevOps

## 🚀 API Endpoints

### 1. Health Check

```bash
GET /api/v1/health
```

Verifica que el servicio está activo.

**Respuesta:**

```json
{
  "status": "UP",
  "service": "Conversor de Divisas",
  "version": "1.0.0"
}
```

### 2. Obtener Todas las Monedas

```bash
GET /api/v1/monedas
```

Retorna todas las monedas disponibles y sus tasas de cambio.

**Respuesta:**

```json
{
  "USD": {
    "codigo": "USD",
    "nombre": "Dólar Estadounidense",
    "simbolo": "$",
    "tasa": 1.0
  },
  "EUR": {
    "codigo": "EUR",
    "nombre": "Euro",
    "simbolo": "€",
    "tasa": 0.92
  }
  // ... más monedas
}
```

### 3. Obtener Información de una Moneda

```bash
GET /api/v1/monedas/{codigo}
```

Ejemplo:

```bash
GET /api/v1/monedas/USD
```

**Respuesta:**

```json
{
  "codigo": "USD",
  "nombre": "Dólar Estadounidense",
  "simbolo": "$",
  "tasa": 1.0
}
```

### 4. Convertir Divisas (POST)

```bash
POST /api/v1/convertir
Content-Type: application/json

{
  "monto": 100,
  "monedaOrigen": "USD",
  "monedaDestino": "EUR"
}
```

**Respuesta:**

```json
{
  "montoOriginal": 100.0,
  "monedaOrigen": "USD",
  "montoConvertido": 92.0,
  "monedaDestino": "EUR",
  "tasa": 0.92,
  "timestamp": 1715338800000
}
```

### 5. Convertir Divisas (GET Simple)

```bash
GET /api/v1/convertir/{monedaOrigen}/{monedaDestino}/{monto}
```

Ejemplo:

```bash
GET /api/v1/convertir/USD/EUR/100
```

### 6. Crear Nueva Moneda (en memoria)

```bash
POST /api/v1/monedas
Content-Type: application/json

{
  "codigo": "PEN",
  "nombre": "Sol Peruano",
  "simbolo": "S/",
  "tasa": 3.75
}
```

- `201 Created` si se guarda.
- `409 Conflict` si el código ya existe.
- `400 Bad Request` si los datos son inválidos (código vacío, tasa ≤ 0, etc.).

### 7. Actualizar Moneda

```bash
PUT /api/v1/monedas/{codigo}
Content-Type: application/json

{
  "codigo": "EUR",
  "nombre": "Euro Actualizado",
  "simbolo": "€",
  "tasa": 0.95
}
```

- `200 OK` con la moneda actualizada.
- `404 Not Found` si el código no existe.

### 8. Eliminar Moneda

```bash
DELETE /api/v1/monedas/{codigo}
```

Respuesta:

```json
{
  "mensaje": "Moneda eliminada",
  "codigo": "PEN"
}
```

### 9. Historial de Conversiones

```bash
GET /api/v1/convertir/historial
```

Devuelve un arreglo con todas las conversiones realizadas desde que arrancó
la aplicación (almacenadas en memoria).

Para limpiar el historial:

```bash
DELETE /api/v1/convertir/historial
```

### 10. Estadísticas

```bash
GET /api/v1/estadisticas
```

**Respuesta:**

```json
{
  "totalMonedas": 9,
  "totalConversiones": 3,
  "usoPorMoneda": {
    "USD": 2,
    "EUR": 1
  },
  "monedaMasUsada": "USD"
}
```

## 💰 Monedas Soportadas

| Código | Nombre | Símbolo | Tasa (vs USD) |
| ------- | ------- | --------- | --------------- |
| USD | Dólar Estadounidense | $ | 1.0 |
| EUR | Euro | € | 0.92 |
| GBP | Libra Esterlina | £ | 0.79 |
| CLP | Peso Chileno | $ | 950.0 |
| MXN | Peso Mexicano | $ | 17.5 |
| ARS | Peso Argentino | $ | 900.0 |
| BRL | Real Brasileño | R$ | 5.0 |
| JPY | Yen Japonés | ¥ | 150.0 |
| BOB | Boliviano | Bs | 6.96 |

## 🛠️ Requisitos Previos

- Java 21 JDK
- Maven 3.9+
- Git

## 📦 Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone https://github.com/timy777/ConversorDivisas.git
cd ConversorDivisas
```

### 2. Ejecutar el Proyecto

```bash
./mvnw spring-boot:run
```

O en Windows:

```bash
mvnw.cmd spring-boot:run
```

La aplicación se ejecutará en `http://localhost:8080`

### 3. Ejecutar Tests

```bash
./mvnw test
```

### 4. Compilar

```bash
./mvnw clean compile
```

### 5. Empaquetar

```bash
./mvnw clean package
```

## 🧪 Pruebas con cURL

### Health Check

```bash
curl http://localhost:8080/api/v1/health
```

### Obtener monedas

```bash
curl http://localhost:8080/api/v1/monedas
```

### Obtener una moneda específica

```bash
curl http://localhost:8080/api/v1/monedas/USD
```

### Convertir USD a EUR (GET)

```bash
curl http://localhost:8080/api/v1/convertir/USD/EUR/100
```

### Convertir USD a EUR (POST)

```bash
curl -X POST http://localhost:8080/api/v1/convertir \
  -H "Content-Type: application/json" \
  -d '{
    "monto": 100,
    "monedaOrigen": "USD",
    "monedaDestino": "EUR"
  }'
```

## 🔄 CI/CD

### GitHub Actions

El proyecto incluye pipeline automático que:

1. ✅ Compila el código
2. ✅ Ejecuta tests unitarios
3. ✅ Genera reportes de cobertura

### Azure DevOps

Configuración en `azure-pipelines.yml` que incluye:

1. **Build**: Compilación con Maven
2. **Test**: Ejecución de tests
3. **Package**: Generación de JAR
4. **Deploy**: Despliegue a Azure App Service

## 🔧 Estructura del Proyecto

```text
ConversorDivisas/
├── src/
│   ├── main/
│   │   ├── java/com/conversor/divisas/diplomado/
│   │   │   ├── controller/
│   │   │   │   └── ConversionController.java
│   │   │   ├── service/
│   │   │   │   └── ConversionService.java
│   │   │   ├── model/
│   │   │   │   ├── Moneda.java
│   │   │   │   ├── ConversionRequest.java
│   │   │   │   └── ConversionResponse.java
│   │   │   └── DiplomadoApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/conversor/divisas/diplomado/
│           ├── ConversionControllerTests.java
│           ├── ConversionServiceTests.java
│           └── DiplomadoApplicationTests.java
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── pom.xml
├── mvnw
├── mvnw.cmd
├── azure-pipelines.yml
└── README.md
```

## 📊 Tests

### Ejecutar todos los tests

```bash
./mvnw clean test
```

### Tests Disponibles (41 tests en total)

**`ConversionServiceTests` (22 tests unitarios):**

- ✅ Obtener catálogo de monedas
- ✅ Obtener moneda existente / inexistente
- ✅ Convertir USD ↔ EUR ↔ GBP (cadena y directa)
- ✅ Conversión a la misma moneda
- ✅ Case insensitive (`usd` == `USD`)
- ✅ Conversión con moneda inválida lanza excepción
- ✅ Crear moneda nueva (PEN)
- ✅ Crear moneda con código en minúsculas se normaliza
- ✅ Crear moneda duplicada falla
- ✅ Crear moneda con tasa inválida falla
- ✅ Actualizar moneda existente
- ✅ Actualizar moneda inexistente falla
- ✅ Eliminar moneda existente / inexistente
- ✅ Historial se registra tras cada conversión
- ✅ Limpiar historial
- ✅ Estadísticas iniciales (0 conversiones)
- ✅ Estadísticas calculan la moneda más usada

**`ConversionControllerTests` (18 tests de API con MockMvc):**

- ✅ `GET /api/v1/health`
- ✅ `GET /api/v1/monedas` y `GET /api/v1/monedas/{codigo}`
- ✅ `GET /api/v1/monedas/{codigo}` con código inexistente → 404
- ✅ `POST /api/v1/convertir` (cuerpo JSON)
- ✅ `GET /api/v1/convertir/{origen}/{destino}/{monto}`
- ✅ Convertir con monto negativo → 400
- ✅ Convertir con moneda inexistente → 400
- ✅ `POST /api/v1/monedas` crea moneda → 201
- ✅ `POST /api/v1/monedas` duplicada → 409
- ✅ `POST /api/v1/monedas` con datos inválidos → 400
- ✅ `PUT /api/v1/monedas/{codigo}` actualiza moneda
- ✅ `PUT /api/v1/monedas/{codigo}` inexistente → 404
- ✅ `DELETE /api/v1/monedas/{codigo}` elimina moneda
- ✅ `DELETE /api/v1/monedas/{codigo}` inexistente → 404
- ✅ `GET /api/v1/convertir/historial` registra conversiones
- ✅ `DELETE /api/v1/convertir/historial` limpia historial
- ✅ `GET /api/v1/estadisticas` reporta moneda más usada

## 🚀 Despliegue en Azure

### Prerequisitos

1. Suscripción de Azure
2. Azure DevOps Project
3. Azure App Service creado

### Pasos

1. Conectar el repositorio GitHub con Azure DevOps
2. Crear el pipeline desde `azure-pipelines.yml`
3. Configurar variables:
   - `AZURE_SUBSCRIPTION`: Azure Subscription Connection
   - `AZURE_APP_NAME`: Nombre del App Service
4. Ejecutar el pipeline

## ✅ Validación CI (Evidencia de Entrega)

### Resultado local validado

- `java -version` y `javac -version`: `openjdk 21.0.11`
- `./mvnw clean test`: `41 tests`, `0 failures`, `BUILD SUCCESS`
- Build del proyecto exitoso con Maven en entorno local

### Evidencia en GitHub Actions

1. Ir a la pestaña **Actions** del repositorio.
2. Abrir la última ejecución del workflow.
3. Verificar que los jobs de build/test estén en estado **Success**.
4. Adjuntar captura que incluya:
   - `commit SHA`
   - rama (`branch`)
   - estado exitoso del workflow
   - paso de tests en verde

### Evidencia en Azure DevOps

1. Ir a **Pipelines > Runs**.
2. Ejecutar pipeline manual sobre la rama subida.
3. Verificar stages en verde:
   - `Build`
   - `Test`
   - `Package`
   - `Deploy` (solo cuando la rama sea `main`)
4. Adjuntar captura del resumen del run y del detalle del stage `Test`.

### Texto sugerido para informe/entrega

> Se validó el proyecto localmente con Java 21 (`openjdk 21.0.11`).
> La suite automática se ejecutó con `./mvnw clean test`, obteniendo
> 41 pruebas exitosas y 0 fallos (22 unitarias de servicio + 18 de API REST
> con MockMvc + 1 de contexto Spring).
> Luego se subieron cambios al repositorio y se ejecutó el pipeline CI en
> remoto, confirmando compilación, pruebas y empaquetado en estado exitoso.

### Checklist final de entrega

- [ ] Link del repositorio
- [ ] Captura de GitHub Actions en estado **Success**
- [ ] Captura de Azure Pipeline en estado **Success**
- [ ] Evidencia local (`java -version`, `javac -version`, `./mvnw clean test`)
- [ ] Evidencia de participación del equipo (commits/PR por integrante)

## ☁️ Análisis de Despliegue Cloud (Práctica 2)

### Comando de ejecución local validado

```bash
./mvnw clean package -DskipTests
java -jar target/diplomado-0.0.1-SNAPSHOT.jar
```

La aplicación queda disponible en `http://localhost:8080`.

### ¿Cómo se desplegaría esta aplicación en la nube?

La aplicación es un microservicio REST Spring Boot empaquetado como JAR
ejecutable. Las opciones de despliegue evaluadas:

1. **Azure App Service for Java** (recomendado para esta práctica):
   - Subir el artefacto `target/diplomado-0.0.1-SNAPSHOT.jar`.
   - Runtime: `JAVA|21` en App Service Linux.
   - Pipeline ya configurado en `azure-pipelines.yml` (stage `Deploy`).
2. **Contenedor Docker** sobre Azure Container Apps / AWS ECS / Google Cloud Run:
   - Imagen base `eclipse-temurin:21-jre`.
   - Auto-escalado por demanda y pago por uso.
3. **AWS Elastic Beanstalk** con plataforma Corretto 21 (similar a App Service).

### ¿Qué modelo: IaaS, PaaS o SaaS?

**Modelo elegido: PaaS (Platform as a Service)**.

| Modelo | ¿Aplica? | Justificación |
| ------ | -------- | ------------- |
| IaaS | ❌ | Implicaría administrar VM, parches del SO y JRE manualmente; sobrecarga innecesaria para una API stateless. |
| **PaaS** | ✅ | Azure App Service / AWS Beanstalk gestionan SO, JVM, escalado horizontal, certificados HTTPS, logs y zero-downtime deploys. Solo subimos el JAR. |
| SaaS | ❌ | No corresponde: no estamos consumiendo un software terminado, estamos publicando uno propio. |

### Consideraciones para producción

- Las tasas y el historial están **en memoria**: para producción reemplazar
  por una base de datos (PostgreSQL en Azure Database, por ejemplo) y un
  caché tipo Redis para las tasas obtenidas de una API externa.
- Habilitar Application Insights / CloudWatch para métricas.
- Configurar variables de entorno (`SPRING_PROFILES_ACTIVE`, credenciales)
  en el App Service en lugar de en `application.properties`.

## 📝 Notas

- Las tasas de cambio son simuladas (en producción usar API externa)
- Todos los montos deben ser mayores a 0
- Los códigos de moneda son case-insensitive
- Timestamp en milisegundos UTC
- El catálogo de monedas y el historial se almacenan **en memoria**, por lo
  que se reinician al reiniciar la aplicación

## 👥 Contribuidores

- **Abelar Alvarez Lijeron**: Creación del proyecto
- **Carlos Mauricio Cabrita Andrade**: Configuración del pipeline
- **Miguel Angel Escobar Lazcano**: Validación de APIs
- **Ronald Nelson Villca Quiroga**: Documentación

## 📄 Licencia

Este proyecto es de carácter educativo para la práctica de Integración Continua.

---

**¿Preguntas o problemas?** Abre un issue en el repositorio.
