package com.grupoaval.avc.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEventDTO {
    private String idCliente;
    private String origen;
    private BigDecimal saldo;
    private BigDecimal prestamo;
    private String tarjetaId;
    private String poliza;
}