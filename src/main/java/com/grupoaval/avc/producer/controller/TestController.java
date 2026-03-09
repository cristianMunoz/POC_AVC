package com.grupoaval.avc.producer.controller;

import com.grupoaval.avc.producer.dto.ResumenProductosDTO;
import com.grupoaval.avc.producer.service.ClienteService;
import com.grupoaval.avc.producer.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final KafkaProducerService producerService;
    private final ClienteService clienteService;

    public TestController(KafkaProducerService producerService, ClienteService clienteService) {
        this.producerService = producerService;
        this.clienteService = clienteService;
    }

    @PostMapping("/flexcube")
    public String dispararFlexCube(@RequestParam String idCliente, @RequestParam String saldo) {
        producerService.enviarEventoFlexCube(idCliente, saldo);
        return "Evento FlexCube enviado";
    }

    @PostMapping("/iseries")
    public String dispararISeries(@RequestParam String idCliente, @RequestParam String monto) {
        // Ya no recibimos el estado desde el Web Service
        producerService.enviarEventoISeries(idCliente, monto);
        return "Abono iSeries enviado: " + monto;
    }

    @PostMapping("/thoughtmachine")
    public String dispararThoughtMachine(@RequestParam String idCliente, @RequestParam String tarjetaId) {
        producerService.enviarEventoThoughtMachine(idCliente, tarjetaId);
        return "Evento ThoughtMachine enviado";
    }

    @PostMapping("/soap")
    public String dispararSoap(@RequestParam String idCliente, @RequestParam String poliza) {
        producerService.enviarEventoSoap(idCliente, poliza);
        return "Evento SOAP enviado";
    }

    @GetMapping("/resumen/{idCliente}")
    public ResumenProductosDTO obtenerResumen(@PathVariable String idCliente) {
        return clienteService.generarResumen(idCliente);
    }
}