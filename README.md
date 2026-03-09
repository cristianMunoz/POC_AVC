# POC AVC - Agregador "Fuente de Verdad" (Versión Base)

Esta solución implementa un Agregador de Datos en tiempo real para el proyecto AVC, unificando información de 4 sistemas legados en una "Sábana de Datos" dinámica y precisa.

## 🧠 Contexto de la Solución
El objetivo es consolidar la información financiera de un cliente proveniente de fuentes heterogéneas (FlexCube, iSeries, ThoughtMachine y SOAP) en una única Vista 360. La lógica de negocio y el estado financiero del cliente se calculan íntegramente en el Backend para garantizar la integridad de los datos.

---

## 🛠️ Stack Tecnológico
* Java 17 y Spring Boot 3.x
* Apache Kafka 4.1.1: Bus de eventos para la comunicación asíncrona
* H2 Database: Base de datos en memoria para persistencia de la "Sábana de Datos"
* Lombok / Jackson: Gestión de POJOs y serialización JSON.

---

## ⚙️ Configuración e Instalación

### 1. Requisitos Previos
* Tener un cluster de Kafka corriendo localmente en el puerto 9092.

### 2. Preparación de Kafka
Antes de subir la app, crea los tópicos necesarios para los sistemas legados ejecutando estos comandos en tu terminal de Kafka:

kafka-topics.sh --create --topic tx-flexcube --bootstrap-server localhost:9092
kafka-topics.sh --create --topic tx-iseries --bootstrap-server localhost:9092
kafka-topics.sh --create --topic tx-thoughtmachine --bootstrap-server localhost:9092
kafka-topics.sh --create --topic tx-soap --bootstrap-server localhost:9092

### 3. Ejecución
Para iniciar el proyecto, usa el siguiente comando de Maven:
./mvnw spring-boot:run

La aplicación estará disponible en http://localhost:8080.

---

## 📐 Reglas de Negocio (Gobernanza de Datos)

1. Precisión Bancaria: Se utiliza BigDecimal en todas las operaciones para evitar errores de coma flotante y la notación científica en los JSON de respuesta.
2. Cálculo de Balance Neto:
    * Activos: Los saldos de FlexCube, ThoughtMachine y SOAP se suman al patrimonio global.
    * Pasivos: Los préstamos de iSeries se restan siempre del saldo global.
3. Estado de Mora Automático: El Back-end determina el estado. Si la deuda en iSeries es mayor a cero (> 0), el producto se marca automáticamente como "MORA".
4. Inicialización por Evento: Los productos solo aparecen en el resumen si han recibido al menos un evento. El primer evento define el monto inicial del producto.
5. Lógica de Billetera: Los eventos de iSeries posteriores a la inicialización se tratan como pagos/abonos que reducen la deuda acumulada.

---

## 🧪 Guía de Pruebas (curl)

Sigue este orden para validar la lógica financiera en el endpoint: GET /api/test/resumen/777

1. Inicializar Ahorros (FlexCube):
   curl.exe -X POST "http://localhost:8080/api/test/flexcube?idCliente=777&saldo=5000000"

2. Crear Deuda Inicial (iSeries):
   curl.exe -X POST "http://localhost:8080/api/test/iseries?idCliente=777&monto=3000000"

3. Realizar Abono a Deuda (Sin parámetro de estado):
   curl.exe -X POST "http://localhost:8080/api/test/iseries?idCliente=777&monto=1000000"

---

## 📂 Estructura de Clases Clave
* KafkaConsumerService: El Agregador que orquesta la entrada de datos y la acumulación de saldos.
* ClienteService: El motor de cálculo que aplica las reglas de balance y determina la Mora.
* ClienteEntity: Nuestra "Sábana de Datos" persistida en H2.