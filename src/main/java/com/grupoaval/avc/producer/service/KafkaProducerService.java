package com.grupoaval.avc.producer.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void enviarEventoFlexCube(String idCliente, String saldo) {
        String json = String.format("{\"idCliente\":\"%s\", \"origen\":\"FlexCube\", \"saldo\":%s}", idCliente, saldo);
        kafkaTemplate.send("tx-flexcube", json);
    }

    public void enviarEventoISeries(String idCliente, String monto) {
        // El JSON ahora es más limpio, solo lleva el monto del movimiento
        String json = String.format("{\"idCliente\":\"%s\", \"origen\":\"iSeries\", \"prestamo\":%s}", idCliente, monto);
        kafkaTemplate.send("tx-iseries", json);
    }

    public void enviarEventoThoughtMachine(String idCliente, String tarjetaId) {
        String json = String.format("{\"idCliente\":\"%s\", \"origen\":\"ThoughtMachine\", \"tarjetaId\":\"%s\"}", idCliente, tarjetaId);
        kafkaTemplate.send("tx-thoughtmachine", json);
    }

    public void enviarEventoSoap(String idCliente, String poliza) {
        String json = String.format("{\"idCliente\":\"%s\", \"origen\":\"SOAP-Seguros\", \"poliza\":\"%s\"}", idCliente, poliza);
        kafkaTemplate.send("tx-soap", json);
    }
}