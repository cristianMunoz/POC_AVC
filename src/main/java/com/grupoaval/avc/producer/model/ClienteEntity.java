package com.grupoaval.avc.producer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEntity {

    @Id
    private String idCliente;

    private String nombre;

    // 1. Fuente: FlexCube (Cuentas de Ahorros)
    private BigDecimal saldoFlexcube;

    // 2. Fuente: iSeries (Préstamos / Cartera)
    private BigDecimal prestamoIseries;

    // 3. Fuente: Thought Machine (Tarjetas de Crédito / Cloud Core)
    private BigDecimal saldoTarjeta;

    // 4. Fuente: SOAP (Seguros / Pólizas Externas)
    private BigDecimal saldoSeguros;

    /**
     * Helper para asegurar que ningún valor sea null al realizar cálculos,
     * evitando NullPointerExceptions en el Service.
     */
    public void initializeDefaults() {
        if (this.saldoFlexcube == null) this.saldoFlexcube = BigDecimal.ZERO;
        if (this.prestamoIseries == null) this.prestamoIseries = BigDecimal.ZERO;
        if (this.saldoTarjeta == null) this.saldoTarjeta = BigDecimal.ZERO;
        if (this.saldoSeguros == null) this.saldoSeguros = BigDecimal.ZERO;
    }
}