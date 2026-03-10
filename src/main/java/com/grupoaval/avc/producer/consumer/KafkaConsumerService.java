package com.grupoaval.avc.producer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupoaval.avc.producer.dto.ClienteEventDTO;
import com.grupoaval.avc.producer.model.ClienteEntity;
import com.grupoaval.avc.producer.repository.ClienteRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class KafkaConsumerService {

    private final ClienteRepository repository;
    private final ObjectMapper mapper;

    public KafkaConsumerService(ClienteRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = {"tx-flexcube", "tx-iseries", "tx-thoughtmachine", "tx-soap"}, groupId = "grupo-poc-avc")
    public void persistirDato(String mensaje) {
        try {
            ClienteEventDTO evento = mapper.readValue(mensaje, ClienteEventDTO.class);
            ClienteEntity cliente = repository.findById(evento.getIdCliente()).orElse(new ClienteEntity());

            cliente.setIdCliente(evento.getIdCliente());
            if (cliente.getNombre() == null) cliente.setNombre("Cristian Munoz");

            switch (evento.getOrigen()) {
                case "FlexCube" -> {
                    BigDecimal actual = cliente.getSaldoFlexcube() != null ? cliente.getSaldoFlexcube() : BigDecimal.ZERO;
                    cliente.setSaldoFlexcube(actual.add(evento.getSaldo()));
                }
                case "iSeries" -> {
                    BigDecimal deuda = cliente.getPrestamoIseries() != null ? cliente.getPrestamoIseries() : BigDecimal.ZERO;
                    BigDecimal nuevaDeuda = deuda.subtract(evento.getPrestamo());
                    cliente.setPrestamoIseries(nuevaDeuda.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : nuevaDeuda);
                }
                case "ThoughtMachine" -> {
                    BigDecimal deudaTdc = cliente.getSaldoTarjeta() != null ? cliente.getSaldoTarjeta() : BigDecimal.ZERO;
                    BigDecimal nuevaDeudaTdc = deudaTdc.subtract(evento.getPrestamo());
                    cliente.setSaldoTarjeta(nuevaDeudaTdc.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : nuevaDeudaTdc);
                }
                case "SOAP" -> {
                    BigDecimal seguros = cliente.getSaldoSeguros() != null ? cliente.getSaldoSeguros() : BigDecimal.ZERO;
                    cliente.setSaldoSeguros(seguros.add(evento.getPrestamo()));
                }
            }

            repository.save(cliente);

            // Log Profesional en Consola Spring
            System.out.println("\n✨ [DATABASE SINK] Actualización Exitosa");
            System.out.println("✅ Origen: " + evento.getOrigen() + " | Cliente: " + evento.getIdCliente() + " | Estado: PERSISTIDO");

        } catch (Exception e) {
            System.err.println("❌ ERROR EN SINK: " + e.getMessage());
        }
    }
}