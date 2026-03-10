package com.grupoaval.avc.producer.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupoaval.avc.producer.dto.ClienteEventDTO;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.math.BigDecimal;

public class FlinkStreamingJob {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ObjectMapper mapper = new ObjectMapper();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("tx-flexcube", "tx-iseries", "tx-thoughtmachine", "tx-soap")
                .setGroupId("flink-agregador-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        stream.map(json -> mapper.readValue(json, ClienteEventDTO.class))
                .keyBy(ClienteEventDTO::getIdCliente)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                .reduce((e1, e2) -> {
                    BigDecimal v1 = e1.getSaldo() != null ? e1.getSaldo() : (e1.getPrestamo() != null ? e1.getPrestamo() : BigDecimal.ZERO);
                    BigDecimal v2 = e2.getSaldo() != null ? e2.getSaldo() : (e2.getPrestamo() != null ? e2.getPrestamo() : BigDecimal.ZERO);

                    if (e1.getSaldo() != null) e1.setSaldo(v1.add(v2));
                    else e1.setPrestamo(v1.add(v2));
                    return e1;
                })
                .map(evento -> {
                    BigDecimal monto = evento.getSaldo() != null ? evento.getSaldo() : evento.getPrestamo();
                    String color = (monto.compareTo(BigDecimal.ZERO) < 0) ? ANSI_RED : ANSI_GREEN;
                    String operacion = (monto.compareTo(BigDecimal.ZERO) < 0) ? "🔻 CARGO" : "🔺 ABONO";

                    return String.format(
                            "\n%s========================================%s" +
                                    "\n%s🏛️  SISTEMA: %-15s | 👤 ID: %s%s" +
                                    "\n%s%s | MONTO: $%,.2f%s" +
                                    "\n%s========================================%s",
                            ANSI_CYAN, ANSI_RESET,
                            ANSI_CYAN, evento.getOrigen(), evento.getIdCliente(), ANSI_RESET,
                            color, operacion, monto, ANSI_RESET,
                            ANSI_CYAN, ANSI_RESET
                    );
                })
                .print();

        env.execute("Flink Engine - Vista 360 Visual");
    }
}