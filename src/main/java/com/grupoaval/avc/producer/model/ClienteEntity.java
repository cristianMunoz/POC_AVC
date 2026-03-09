package com.grupoaval.avc.producer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class ClienteEntity {
    @Id
    private String idCliente;
    private String nombre;

    // Todos inician en null para detectar el "Primer Evento"
    private BigDecimal saldoFlexcube;      // Ahorros
    private BigDecimal prestamoIseries;    // Deuda
    private BigDecimal saldoThoughtMachine; // Tarjetas
    private BigDecimal saldoSoap;          // Seguros/Primas

    // Identificadores de sistemas legados
    private String tarjetaId;
    private String polizaId;
}