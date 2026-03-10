# 🚀 POC AVC - Arquitectura de Streaming con Apache Flink

## 📋 Descripción del Proyecto
Esta Prueba de Concepto (POC) demuestra la implementación de una **Vista 360 de Cliente** en tiempo real. Utiliza una arquitectura orientada a eventos donde múltiples fuentes de verdad (Sistemas Legados y Cloud) convergen en un motor de procesamiento distribuido (**Apache Flink**) para consolidar saldos y estados financieros mediante **aritmética de signos**.



---

## 🏗️ Arquitectura Técnica
La solución se divide en tres capas principales que garantizan el desacoplamiento y la escalabilidad del sistema:

1. **Productores (Spring Boot API):** Actúan como gateways para simular la entrada de datos de 4 sistemas core bancarios:
   - **FlexCube:** Gestión de Cuentas de Ahorros.
   - **iSeries:** Core de Préstamos y Cartera.
   - **Thought Machine:** Core bancario Cloud-Native (Tarjetas).
   - **SOAP:** Integración con sistemas de Seguros externos.

2. **Motor de Agregación (Apache Flink):** El componente de procesamiento distribuido. Procesa los flujos de Kafka en ventanas de tiempo de **5 segundos**, consolidando los montos (Cargos/Abonos) de forma asíncrona.

3. **Consumidor y Sink (Spring Data + H2):** Recibe los datos agregados por Flink, actualiza la entidad del cliente en la base de datos H2 y expone los resultados mediante una API REST para el canal digital.

---

## 🛠️ Tecnologías y Requerimientos
* **Java 17** (LTS)
* **Spring Boot 3.x**
* **Apache Flink 1.15+** (Streaming Environment)
* **Apache Kafka** (Broker local corriendo en puerto 9092)
* **H2 Database** (Consola habilitada en `/h2-console`)
* **Lombok & Jackson** (Serialización y reducción de boilerplate)

---

## 🚀 Guía de Instalación y Ejecución

### 1. Preparación de Infraestructura
Asegúrese de tener **Kafka** activo localmente. Los tópicos requeridos son:
- `tx-flexcube`, `tx-iseries`, `tx-thoughtmachine`, `tx-soap`.

### 2. Paso a Paso para la Ejecución
Para que la POC funcione correctamente, es vital seguir este orden de inicio:

1. **Iniciar el Motor Flink:**
   - Ejecute la clase `com.grupoaval.avc.producer.flink.FlinkStreamingJob`.
   - Verificará que el motor está activo al ver los bordes cian en la consola.

2. **Iniciar Microservicio Spring Boot:**
   - Ejecute la clase `PocAvcProducerApplication`.
   - La API se desplegará en el puerto `8080`.

---

## 🧪 Pruebas de Funcionamiento (Aritmética de Signos)

El sistema utiliza una lógica de signos para unificar el consumo de diversas fuentes sin necesidad de campos adicionales:
* **Valor POSITIVO (+):** Representa un **Abono** o ingreso (Ahorros / Seguros).
* **Valor NEGATIVO (-):** Representa un **Cargo** o deuda (Préstamos / Consumos TDC).



### Batería de Curls para Prueba Integral (ID Cliente: 777)

Ejecute estos comandos en su terminal de PowerShell para validar la integración:

```powershell
# 1. Depósito Inicial en Ahorros
curl.exe -X POST "http://localhost:8080/api/test/flexcube?idCliente=777&saldo=5000000"

# 2. Cargo de Préstamo (Deuda)
curl.exe -X POST "http://localhost:8080/api/test/iseries?idCliente=777&monto=-3000000"

# 3. Consumo con Tarjeta de Crédito
curl.exe -X POST "http://localhost:8080/api/test/thoughtmachine?idCliente=777&monto=-1200000&tarjetaId=VISA-777"

# 4. Pago de Póliza de Seguro
curl.exe -X POST "http://localhost:8080/api/test/soap?idCliente=777&monto=800000&poliza=POL-2026"
```
### 📊 Consulta de la Vista 360 Consolidada
Espere el cierre de la ventana de Flink (máximo 5 segundos) y acceda a:
👉 `GET http://localhost:8080/api/test/resumen/777`

**Resultado Esperado:**
- **Saldo Total Global:** `$1,600,000` 
- **Detalle:** Se listarán los 4 productos con sus respectivos estados (Activa, MORA, Vigente) y sus valores individuales agregados.

---

## 🛡️ Características de Diseño
* **Separación de Responsabilidades:** El cálculo pesado ocurre en Flink; el microservicio solo persiste y sirve datos.
* **Resiliencia:** Implementación de validaciones en el Service para manejar consultas de clientes aún no registrados sin generar excepciones 500.
* **Observabilidad:** Salida de consola decorada con colores ANSI (Verde para abonos, Rojo para cargos) para facilitar el monitoreo visual durante las pruebas de integración.

---
**Desarrollado por:** Cristian Muñoz - Backend Coordinator