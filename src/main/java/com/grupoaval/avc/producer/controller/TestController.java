package com.grupoaval.avc.producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupoaval.avc.producer.dto.ClienteEventDTO;
import com.grupoaval.avc.producer.dto.ResumenProductosDTO;
import com.grupoaval.avc.producer.service.ClienteService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final KafkaTemplate<String, String> kafkaTemplate; // Cambiado a String
    private final ClienteService clienteService;
    private final ObjectMapper objectMapper;

    public TestController(KafkaTemplate<String, String> kafkaTemplate,
                          ClienteService clienteService,
                          ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.clienteService = clienteService;
        this.objectMapper = objectMapper;
    }

    private void enviarEvento(String topico, ClienteEventDTO evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);
            kafkaTemplate.send(topico, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando JSON", e);
        }
    }

    @PostMapping("/flexcube")
    public String emitirFlexcube(@RequestParam String idCliente, @RequestParam BigDecimal saldo) {
        enviarEvento("tx-flexcube", new ClienteEventDTO(idCliente, "FlexCube", saldo, null, null, null));
        return "Evento FlexCube enviado: " + saldo;
    }

    @PostMapping("/iseries")
    public String emitirIseries(@RequestParam String idCliente, @RequestParam BigDecimal monto) {
        enviarEvento("tx-iseries", new ClienteEventDTO(idCliente, "iSeries", null, monto, null, null));
        return "Evento iSeries enviado: " + monto;
    }

    @PostMapping("/thoughtmachine")
    public String emitirThoughtMachine(@RequestParam String idCliente, @RequestParam BigDecimal monto, @RequestParam String tarjetaId) {
        enviarEvento("tx-thoughtmachine", new ClienteEventDTO(idCliente, "ThoughtMachine", null, monto, tarjetaId, null));
        return "Evento Thought Machine enviado: " + monto;
    }

    @PostMapping("/soap")
    public String emitirSoap(@RequestParam String idCliente, @RequestParam BigDecimal monto, @RequestParam String poliza) {
        enviarEvento("tx-soap", new ClienteEventDTO(idCliente, "SOAP", null, monto, null, poliza));
        return "Evento SOAP enviado: " + monto;
    }

    @GetMapping("/resumen/{idCliente}")
    public ResumenProductosDTO obtenerVista360(@PathVariable String idCliente) {
        return clienteService.generarResumen(idCliente);
    }
}