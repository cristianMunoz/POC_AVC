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

    @KafkaListener(topics = {"tx-flexcube", "tx-thoughtmachine", "tx-iseries", "tx-soap"}, groupId = "grupo-poc-avc")
    public void procesarYPersistir(String mensaje) {
        try {
            ClienteEventDTO evento = mapper.readValue(mensaje, ClienteEventDTO.class);
            ClienteEntity cliente = repository.findById(evento.getIdCliente()).orElse(new ClienteEntity());

            cliente.setIdCliente(evento.getIdCliente());
            if (cliente.getNombre() == null) cliente.setNombre("Cristian Munoz");

            // Lógica de Inicialización vs. Actualización para todos los sistemas
            switch (evento.getOrigen()) {
                case "FlexCube" -> {
                    if (cliente.getSaldoFlexcube() == null) {
                        cliente.setSaldoFlexcube(evento.getSaldo()); // Inicializa (puede ser 0)
                    } else {
                        cliente.setSaldoFlexcube(cliente.getSaldoFlexcube().add(evento.getSaldo())); // Acumula
                    }
                }
                case "iSeries" -> {
                    if (cliente.getPrestamoIseries() == null) {
                        cliente.setPrestamoIseries(evento.getPrestamo()); // Inicializa la deuda (puede ser 0)
                    } else {
                        // Trata el evento como un pago (resta)
                        BigDecimal nuevaDeuda = cliente.getPrestamoIseries().subtract(evento.getPrestamo());
                        cliente.setPrestamoIseries(nuevaDeuda.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : nuevaDeuda);
                    }
                }
                case "ThoughtMachine" -> {
                    cliente.setTarjetaId(evento.getTarjetaId());
                    if (cliente.getSaldoThoughtMachine() == null) {
                        cliente.setSaldoThoughtMachine(evento.getSaldo() != null ? evento.getSaldo() : BigDecimal.ZERO);
                    } else if (evento.getSaldo() != null) {
                        cliente.setSaldoThoughtMachine(cliente.getSaldoThoughtMachine().add(evento.getSaldo()));
                    }
                }
                case "SOAP-Seguros" -> {
                    cliente.setPolizaId(evento.getPoliza());
                    if (cliente.getSaldoSoap() == null) {
                        cliente.setSaldoSoap(evento.getSaldo() != null ? evento.getSaldo() : BigDecimal.ZERO);
                    } else if (evento.getSaldo() != null) {
                        cliente.setSaldoSoap(cliente.getSaldoSoap().add(evento.getSaldo()));
                    }
                }
            }

            repository.save(cliente);

        } catch (Exception e) {
            System.err.println("❌ Error en Agregador: " + e.getMessage());
        }
    }
}